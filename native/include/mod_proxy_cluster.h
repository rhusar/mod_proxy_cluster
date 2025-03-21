/*
 * Copyright The mod_cluster Project Authors
 * SPDX-License-Identifier: Apache-2.0
 */

#ifndef MOD_PROXY_CLUSTER_H
#define MOD_PROXY_CLUSTER_H

/**
 * @file  mod_proxy_cluster.h
 * @brief mod_proxy_cluster module for Apache
 * @author Jean-Frederic Clere
 *
 * @defgroup MOD_PROXY_CLUSTER mod_proxy_cluster
 * @ingroup  APACHE_MODS
 * @{
 */

#include "balancer.h"
#include "node.h"
#include "context.h"
#include "host.h"

#define MOD_CLUSTER_EXPOSED_VERSION "mod_cluster/2.0.0.Alpha1-SNAPSHOT"

/* define HAVE_CLUSTER_EX_DEBUG to have extented debug in mod_cluster */
#define HAVE_CLUSTER_EX_DEBUG       0

/* We don't care about versions older then 2.4.x, i.e., MODULE_MAGIC_NUMBER_MAJOR < 20120211 */
#if MODULE_MAGIC_NUMBER_MAJOR == 20120211 && MODULE_MAGIC_NUMBER_MINOR < 124
#error Please update your HTTPD, mod_proxy_cluster requires version 2.4.53 or newer.
#endif

/* BALANCER_PREFIX is defined in mod_proxy */
/* if the prefix chagnes, don't forget to change the warning as well */
#ifndef BALANCER_PREFIX
#define BALANCER_PREFIX "balancer://"
#warning "BALANCER_PREFIX macro was undefined, now set to balancer://"
#endif
#define BALANCER_PREFIX_LENGTH (sizeof(BALANCER_PREFIX) - 1)

struct balancer_method
{
    /**
     * Check that the node is responding
     * @param r request_rec structure
     * @param id ident of the worker
     * @param load load factor to set if test is ok
     * @return 0 in case of success, HTTP_INTERNAL_SERVER_ERROR otherwise
     */
    int (*proxy_node_isup)(request_rec *r, int id, int load);
    /**
     * Check that the node is responding
     * @param r request_rec structure
     * @param scheme something like ajp, http or https
     * @param host the hostname
     * @param port the port on which the node connector is running
     * @return 0 in case of success, HTTP_INTERNAL_SERVER_ERROR otherwise
     */
    int (*proxy_host_isup)(request_rec *r, const char *scheme, const char *host, const char *port);
    /**
     * Check if a worker already exists and return the corresponding id
     * @param r request_rec structure
     * @param balancername, the balancer name
     * @param scheme something like ajp, http or https
     * @param host the hostname
     * @param port the port on which the node connector is running
     * @param id the address to store the index that was previously used
     * @param the_conf adress to store the proxy_server_conf the worker is using
     * @return the worker or NULL if not existing
     */
    proxy_worker *(*proxy_node_getid)(request_rec *r, const char *balancername, const char *scheme, const char *host,
                                      const char *port, int *id, const proxy_server_conf **the_conf);

    /**
     * Re-enable the proxy_worker
     * @param r request_rec structure
     * @param node pointer to node structure we have created
     * @param worker the proxy_worker to re enable
     * @param nodeinfo pointer to node structure we are creating
     * @param the_conf the proxy_server_conf from proxy_node_getid()
     */
    void (*reenable_proxy_worker)(server_rec *s, nodeinfo_t *node, proxy_worker *worker, nodeinfo_t *nodeinfo,
                                  const proxy_server_conf *the_conf);

    /**
     * Get a free id in the node table
     * @param r request_rec whose pool is used for memory allocations
     * @param node_table_size the size of the table
     * @return the first free id in the table or -1 if none exists
     */
    int (*proxy_node_get_free_id)(request_rec *r, int node_table_size);
};

typedef struct balancer_method balancer_method;

/**
 * Context table copy for local use
 */
struct proxy_context_table
{
    int sizecontext;
    int *contexts;
    contextinfo_t *context_info;
};
typedef struct proxy_context_table proxy_context_table;


/**
 * VHost table copy for local use
 */
struct proxy_vhost_table
{
    int sizevhost;
    int *vhosts;
    hostinfo_t *vhost_info;
};
typedef struct proxy_vhost_table proxy_vhost_table;

/**
 * Balancer table copy for local use
 */
struct proxy_balancer_table
{
    int sizebalancer;
    int *balancers;
    balancerinfo_t *balancer_info;
};
typedef struct proxy_balancer_table proxy_balancer_table;

/**
 * Node table copy for local use, the ptr_node is the shared memory address (slotmem address)
 */
struct proxy_node_table
{
    int sizenode;
    int *nodes;
    nodeinfo_t *node_info;
    char **ptr_node;
};
typedef struct proxy_node_table proxy_node_table;

/**
 * Table of node and context selected by find_node_context_host()
 */
struct node_context
{
    int node;
    int context;
};
typedef struct node_context node_context;

#endif /*MOD_PROXY_CLUSTER_H*/
