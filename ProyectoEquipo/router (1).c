#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <ctype.h>

 
void hilo(void *sock){
	int socket_cliente=*((int *)(sock))
	int socket_servidor;
	char buffer[200];
        char cadena[500];
        int nbytes;
	//configuracion para el server
    socket_servidor = socket(AF_INET, SOCK_STREAM, protoent->p_proto);
    
    
    dir_servidor.sin_family = AF_INET;
    dir_servidor.sin_addr.s_addr = INADDR_ANY;
    dir_servidor.sin_port = htons(puerto_servidor);
    
    if (connect(socket_servidor, (struct sockaddr *)&dir_servidor, sizeof(struct sockaddr_in)) == -1) {
        perror("Error en la conexión al router");
        exit(-1);
    }
 
        int tam = sizeof(struct sockaddr_in);
        int enter_presente=0;
        
        //Limpia la cadena
        memset(cadena, 0, 500);
        //El router se detiene a escuchar una conexión entrante a través de la red y devuelve un nuevo socket
       
        while(!enter_presente) {
            //Limpia el buffer
            memset(buffer, 0, 200);
            //Se lee una cadena desde el socket cliente
            nbytes = read(socket_cliente, buffer, 200);
            //Se copia el buffer leido hacia la cadena
            strncat(cadena, buffer, nbytes);
            //Si el buffer contiene un ENTER se termina la lectura
            if (strstr(buffer, "\n"))
                enter_presente = 1;
        }
        //Se calcula la longitud de la cadena completa memset
        nbytes = strlen(cadena);
        //Se imprime la cadena leida desde la conexión
        printf("El cliente ha enviado: %s", cadena);
        
        //Se envia al servido
        write(socket_servidor, cadena, nbytes);


        //se lee del servidor y se envia al cliente
        memset(cadena, 0, 500);
        enter_presente = 0;
        while(!enter_presente) {
            //Limpia el buffer
            memset(buffer, 0, 200);
            //Se lee una cadena desde el socket cliente
            nbytes = read(socket_servidor, buffer, 200);
            //Se copia el buffer leido hacia la cadena
            strncat(cadena, buffer, nbytes);
            //Si el buffer contiene un ENTER se termina la lectura
            if (strstr(buffer, "\n"))
                enter_presente = 1;
        }

        write(socket_cliente, cadena, nbytes);

        //Se cierra el socket cliente 
        close(socket_cliente);
}

int main(int argc, char *argv[]) {
    const char protocolo[]="tcp";
    int socket_router, socket_cliente;
    struct sockaddr_in dir_router, dir_cliente, dir_servidor;
    unsigned short puerto_router = 9090u, puerto_servidor = 4500u;
    struct protoent *protoent;
    int habilitar_reuso_socket=1;
    
    //Obtiene la configuración para el socket TCP
    protoent = getprotobyname(protocolo);

    

    //Crea el socket del dominio Internet, para comunicaciones orientadas a conexión y con el protocolo seleccionado
    socket_router = socket(AF_INET, SOCK_STREAM, protoent->p_proto);
    //Se configura el socket para ser reusado cuando se cierre el programa
    setsockopt(socket_router, SOL_SOCKET, SO_REUSEADDR, &habilitar_reuso_socket, sizeof(int));
    
    //Se configura la dirección en la que escucha el router
    dir_router.sin_family = AF_INET;
    dir_router.sin_addr.s_addr = htonl(INADDR_ANY);
    dir_router.sin_port = htons(puerto_router);
    
    //Se asocia la dirección al socket para escuchar conexiones entrantes
    if (bind(socket_router, (struct sockaddr *)&dir_router, sizeof(struct sockaddr_in)) == -1) {
        perror("Error en la asignación del puerto al socket, el puerto está en uso");
        exit(-1);
    }
    //Se procede a establecer la cola de conexiones entrantes asociada al socket
    listen(socket_router, 5);
    
    while(1) {
 	//vinculado al cliente para poder atenderlo
        socket_cliente = accept(socket_router, (struct sockaddr *) &dir_cliente, &tam);
        pthread_t h1;
	pthread_create(&h1,NULL,hilo,&socket_cliente);
    }
}