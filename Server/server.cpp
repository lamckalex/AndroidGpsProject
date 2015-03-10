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

//defines
#define SERVER_TCP_PORT 7000	// Default port
#define BUFLEN	64 * 1024				//Buffer length

//prototypes

void readFromClient(int client_socket);

int main()
{
	int 	listen_socket, new_socket, retval, client_len;
	struct	sockaddr_in server, client;	
	pid_t	pid;

	//set up a TCP listening socket
	listen_socket = socket(AF_INET, SOCK_STREAM, 0);
	if (listen_socket == -1)
	{
		return 1;
	}

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
	return 0;
}

void readFromClient(int client_socket)
{
	int n, bytes_to_read;
	char	*bp, buf[BUFLEN];
	bp = buf;
	bytes_to_read = BUFLEN;

	//read BUFLEN chars from the port
	while ((n = recv (client_socket, bp, bytes_to_read, 0)) < BUFLEN)
	{
		bp += n;
		bytes_to_read -= n;
	}

	//deserialize data from port

	printf("Debug: contents of buffer: ");
	for (int i = 0; i < BUFLEN; i++)
	{
		printf("%c", bp[i]);
	}

	//write the text to XML

}