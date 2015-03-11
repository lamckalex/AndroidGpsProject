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

//defines
#define SERVER_TCP_PORT 7000	// Default port
#define BUFLEN	1024				//Buffer length

//prototypes

void readFromClient(int client_socket);
void sig_handler (int sig);

int 	listen_socket, new_socket;

int main()
{
	int 	retval, client_len;
	struct	sockaddr_in server, client;	
	pid_t	pid;

	signal(SIGINT, sig_handler);

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

	return 0;
}

void readFromClient(int client_socket)
{
	int n, bytes_to_read;
	char	*bp, buf[BUFLEN];

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
			close(client_socket);
			break;
		}


		//deserialize data from port
		printf("Received: %s\n", buf);
		fflush(stdout);


	}
		
	
	//write the text to XML

	exit(0);
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