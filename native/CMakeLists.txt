#==================================
# mod_cluster native parent project
# @author: Michal Karm Babacek
#==================================

CMAKE_MINIMUM_REQUIRED(VERSION 2.8)
PROJECT(mod_cluster)

IF(DEBUG)
    SET(CMAKE_CX_FLAGS "-g -O2 -Wall -std=C99 -fPIC")
ELSE()
    SET(CMAKE_CX_FLAGS "-O2 -Wall -std=C99 -fPIC")
ENDIF()

SET(LIBRARY_OUTPUT_PATH ${CMAKE_BINARY_DIR}/modules)
SET(PROJECT_INCLUDE_DIR ${PROJECT_SOURCE_DIR}/include)
SET(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake/modules ${CMAKE_MODULE_PATH})

FIND_PACKAGE(APR REQUIRED)
FIND_PACKAGE(APACHE REQUIRED)

INCLUDE_DIRECTORIES(${APR_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${APRUTIL_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${APACHE_INCLUDE_DIR})

INCLUDE_DIRECTORIES("${PROJECT_INCLUDE_DIR}")
INCLUDE_DIRECTORIES("${PROJECT_SOURCE_DIR}")

ADD_SUBDIRECTORY(mod_proxy_cluster)
ADD_SUBDIRECTORY(advertise)
ADD_SUBDIRECTORY(mod_cluster_slotmem)
ADD_SUBDIRECTORY(mod_manager)