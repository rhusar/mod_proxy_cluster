/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.modcluster.catalina;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServer;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;

import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.modcluster.Context;
import org.jboss.modcluster.Host;
import org.jboss.servlet.http.HttpEvent;

/**
 * {@link Context} implementation that wraps a {@link org.apache.catalina.Context}.
 * @author Paul Ferraro
 */
public class CatalinaContext implements Context
{
   private final org.apache.catalina.Context context;
   private final Host host;
   
   /**
    * Constructs a new CatalinaContext wrapping the specified context.
    * @param context the catalina context
    * @param host the parent container
    */
   public CatalinaContext(org.apache.catalina.Context context, Host host)
   {
      this.context = context;
      this.host = host;
   }
   
   /**
    * Constructs a new CatalinaContext wrapping the specified context.
    * @param context the catalina context
    */
   public CatalinaContext(org.apache.catalina.Context context, MBeanServer mbeanServer)
   {
      this(context, new CatalinaHost((org.apache.catalina.Host) context.getParent(), mbeanServer));
   }
   
   /**
    * {@inhericDoc}
    * @see org.jboss.modcluster.Context#getHost()
    */
   public Host getHost()
   {
      return this.host;
   }

   /**
    * {@inhericDoc}
    * @see org.jboss.modcluster.Context#getPath()
    */
   public String getPath()
   {
      return this.context.getPath();
   }

   /**
    * {@inhericDoc}
    * @see org.jboss.modcluster.Context#isStarted()
    */
   public boolean isStarted()
   {
      try
      {
         return this.context.isStarted();
      }
      catch (NoSuchMethodError e)
      {
         return true;
      }
   }
   
   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.Context#addRequestListener(javax.servlet.ServletRequestListener)
    */
   public void addRequestListener(final ServletRequestListener listener)
   {
      // Add a valve rather than using Context.setApplicationEventListeners(...), since these will be overwritten at the end of Context.start()
      this.context.getPipeline().addValve(new RequestListenerValve(listener));
   }
   
   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.Context#removeRequestListener(javax.servlet.ServletRequestListener)
    */
   public void removeRequestListener(ServletRequestListener listener)
   {
      Valve listenerValve = new RequestListenerValve(listener);
      
      Pipeline pipeline = this.context.getPipeline();
      
      for (Valve valve: pipeline.getValves())
      {
         if (listenerValve.equals(valve))
         {
            pipeline.removeValve(valve);
            
            return;
         }
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.Context#getActiveSessionCount()
    */
   public int getActiveSessionCount()
   {
      return this.context.getManager().getActiveSessions();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.Context#isDistributable()
    */
   public boolean isDistributable()
   {
      return this.context.getManager().getDistributable();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.SessionManager#addSessionListener(javax.servlet.http.HttpSessionListener)
    */
   public void addSessionListener(HttpSessionListener listener)
   {
      synchronized (this.context)
      {
         this.context.setApplicationLifecycleListeners(this.addListener(listener, this.context.getApplicationLifecycleListeners()));
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.modcluster.SessionManager#removeSessionListener(javax.servlet.http.HttpSessionListener)
    */
   public void removeSessionListener(HttpSessionListener listener)
   {
      synchronized (this.context)
      {
         this.context.setApplicationLifecycleListeners(this.removeListener(listener, this.context.getApplicationLifecycleListeners()));
      }
   }
   
   private Object[] addListener(Object listener, Object[] listeners)
   {
      if (listeners == null)
      {
         return new Object[] { listener };
      }
      
      List<Object> listenerList = new ArrayList<Object>(listeners.length + 1);
      
      listenerList.add(listener);
      listenerList.addAll(Arrays.asList(listeners));
      
      return listenerList.toArray();
   }

   private Object[] removeListener(Object listener, Object[] listeners)
   {
      if (listeners == null) return null;
      
      List<Object> listenerList = new ArrayList<Object>(listeners.length - 1);

      for (Object existingListener: listeners)
      {
         if (!existingListener.equals(listener))
         {
            listenerList.add(existingListener);
         }
      }
      
      return listenerList.toArray();
   }
   
   /**
    * {@inheritDoc}
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object object)
   {
      if ((object == null) || !(object instanceof CatalinaContext)) return false;
      
      CatalinaContext context = (CatalinaContext) object;
      
      return this.context == context.context;
   }

   /**
    * {@inheritDoc}
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return this.context.hashCode();
   }

   /**
    * {@inhericDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return this.context.getPath();
   }
   
   /**
    * A valve that notifies a request listener.
    */
   private static class RequestListenerValve extends ValveBase
   {
      private final ServletRequestListener listener;
      
      RequestListenerValve(ServletRequestListener listener)
      {
         this.listener = listener;
      }
      
      @Override
      public void invoke(Request request, Response response) throws IOException, ServletException
      {
         this.event(request, response, null);
      }
      
      /**
       * {@inheritDoc}
       * @see org.apache.catalina.valves.ValveBase#event(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response, org.jboss.servlet.http.HttpEvent)
       */
      @Override
      public void event(Request request, Response response, HttpEvent event) throws IOException, ServletException
      {
         ServletRequestEvent requestEvent = new ServletRequestEvent(request.getContext().getServletContext(), request);
         
         this.listener.requestInitialized(requestEvent);
         
         Valve valve = this.getNext();
         
         try
         {
            if (event != null)
            {
               valve.event(request, response, event);
            }
            else
            {
               valve.invoke(request, response);
            }
         }
         finally
         {
            this.listener.requestDestroyed(requestEvent);
         }
      }

      /**
       * {@inheritDoc}
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return this.listener.hashCode();
      }

      /**
       * {@inheritDoc}
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object object)
      {
         if ((object == null) || !(object instanceof RequestListenerValve)) return false;
         
         RequestListenerValve valve = (RequestListenerValve) object;
         
         return this.listener == valve.listener;
      }
   }
}