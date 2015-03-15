#ifndef SERVER_H
#define SERVER_H


#include <cstdio>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <cstring>
#include <arpa/inet.h>
#include <cstdlib>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <string>
#include <string.h>

#include "xmlmanip.h"

//defines
#define SERVER_TCP_PORT 7000	// Default port
#define BUFLEN	1024				//Buffer length

struct pdata
{
  char* plong;
  char* plat;
  char* pip;
  char* ptime;
  char* pmac;
  char* pname;
};

//prototypes

void readFromClient(int client_socket);
pdata rawToPData(char* str);
void sig_handler (int sig);
Location pDataToLocation (pdata data);

#endif