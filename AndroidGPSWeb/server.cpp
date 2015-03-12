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
};

//prototypes

void readFromClient(int client_socket);
pdata rawToPData(char* str);
void sig_handler (int sig);
Location pDataToLocation (pdata data);

int listen_socket, new_socket;
pdata *packet;

int main()
{
	int 	retval, client_len, status;
	struct	sockaddr_in server, client;	
	pid_t	pid;

	signal(SIGINT, sig_handler);

	packet = (pdata*)malloc(sizeof(pdata));

	//set up a TCP listening socket
	listen_socket = socket(AF_INET, SOCK_STREAM, 0);
	if (listen_socket == -1)
	{
		return 1;
	}

	printf("Start server\n");

	//set up sockaddr_in struct
	bzero((char *)&server, sizeof(struct sockaddr_in));
	server.sin_family = AF_INET;
	server.sin_port = htons(SERVER_TCP_PORT);
	server.sin_addr.s_addr = htonl(INADDR_ANY); // Accept connections from any client

	// bind the socket to accept connections from any address
	retval = bind (listen_socket, (struct sockaddr *)&server, sizeof(server));
	if ( retval == -1)
	{
		return 1;
	}

	//listen for connections
	// queue up to 5 connect requests
	listen(listen_socket, 5);

	printf("Listening on socket: %d\n", listen_socket);


	//go into a read loop
	for(;;)
	{
		client_len = sizeof(client);

		//if a new connection occurs, start a child process and create a new socket and
		new_socket = accept (listen_socket, (struct sockaddr *)&client, (socklen_t*)&client_len);
		if ( new_socket == -1 )
		{
			return 1;
		}

		printf("Accepted connection: %d\n", new_socket);

		//create a child process to handle the new socket
		pid = fork();

		if (pid == -1)
		{
			//fork failed
			return 1;
		}
		else if ( pid == 0)
		{
			//child code
			readFromClient(new_socket);

		}
	}

	close(listen_socket);
	close(new_socket);

	wait(&status);
	return 0;
}

void readFromClient(int client_socket)
{
	int n, bytes_to_read;
	char	*bp, buf[BUFLEN];

	memset(buf, 0, sizeof(buf));

	fflush(stdout);
	
	bp = buf;
	bytes_to_read = BUFLEN;

	while(true)
	{

		//read BUFLEN chars from the port
		n = recv (client_socket, bp, bytes_to_read, 0);
		
		if (n == 0)
		{
			printf("Socket: %d disconnected\n", client_socket);
			fflush(stdout);
			close(client_socket);
			close(new_socket);
			break;
		}


		pdata data = rawToPData(buf);
		fflush(stdout);

		//convert PData to a Location struct
		Location location = pDataToLocation(data);

		//read all the entries from the XML document to a vector of locations
  		std::ifstream input("coordinates.xml");
  		Locations locations = readXML( input );

		//add the new location to the vector
		locations.push_back(location);

		//write to XML
		std::ofstream output("coordinates.xml");
  		writeXML( locations, output );

	}

	return;
}

/*
Alex's string to char* parser
*/
pdata rawToPData(char* str)
{
	pdata p;

	char * pch;
	printf ("Splitting string \"%s\" into tokens:\n",str);

	pch = strtok (str,",");
	p.plong = pch;

	pch = strtok (NULL, ",");
	p.plat = pch;

	pch = strtok (NULL, ",");
	p.pip = pch;

	pch = strtok (NULL, ",");
	p.ptime = pch;

	pch = strtok (NULL, ",");

	printf("%s\n", p.plong);
	printf("%s\n", p.plat);
	printf("%s\n", p.pip);
	printf("%s\n", p.ptime);

	return p;

}

/*

*/

Location pDataToLocation (pdata data)
{
	Location new_location;

	//convert characters to doubles
	double new_longitude = atof(data.plong);
	double new_latitude =  atof(data.plat);


	new_location.longitude = new_longitude;
	new_location.latitude = new_latitude;
	new_location.ip_address = data.pip;
	new_location.time = data.ptime;

	return new_location;
}


/*******************************************************************************************************
** Function: 	sig_handler
**
** DATE:		February 11, 2015
**
** REVISIONS:	
**
** DESIGNER:	Filip Gutica A00781910
**
** PROGRAMMER:	Filip Gutica A00781910
**
** INTERFACE:	void server(int qid, long type, struct msgbuf qbuf)
**
** Params:		sig 	-signal to be handled
**
** RETURNS: void
**
** NOTES:
** Removes the Message Queue on a SIGINT signal
******************************************************************************************************/
void sig_handler (int sig)
{
	int status;

	if (sig == SIGINT)
	{
		/* Remove he message queue */
		close(listen_socket);
		close(new_socket);

		wait(&status);
		exit(0);
	}
}