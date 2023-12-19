#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <ctype.h>

 


int main(int argc, char *argv[]) {
    const char protocolo[]="tcp";
    int socket_servidor, socket_router;
    struct sockaddr_in dir_servidor, dir_router;
    unsigned short puerto_servidor = 4500u;
    struct protoent *protoent;
    int habilitar_reuso_socket=1;
    
    //Obtiene la configuración para el socket TCP
    protoent = getprotobyname(protocolo);

 

    //Crea el socket del dominio Internet, para comunicaciones orientadas a conexión y con el protocolo seleccionado
    socket_servidor = socket(AF_INET, SOCK_STREAM, protoent->p_proto);
    //Se configura el socket para ser reusado cuando se cierre el programa
    setsockopt(socket_servidor, SOL_SOCKET, SO_REUSEADDR, &habilitar_reuso_socket, sizeof(int));
    
    //Se configura la dirección en la que escucha el servidor
    dir_servidor.sin_family = AF_INET;
    dir_servidor.sin_addr.s_addr = htonl(INADDR_ANY);
    dir_servidor.sin_port = htons(puerto_servidor);
    
    //Se asocia la dirección al socket para escuchar conexiones entrantes
    if (bind(socket_servidor, (struct sockaddr *)&dir_servidor, sizeof(struct sockaddr_in)) == -1) {
        perror("Error en la asignación del puerto al socket, el puerto está en uso");
        exit(-1);
    }
    //Se procede a establecer la cola de conexiones entrantes asociada al socket
    listen(socket_servidor, 5);
    
    while(1) {
        char buffer[200];
        char cadena[500];
        int nbytes;
        int tam = sizeof(struct sockaddr_in);
        int enter_presente=0;
        
        //Limpia la cadena
        memset(cadena, 0, 500);
        //El servidor se detiene a escuchar una conexión entrante a través de la red y devuelve un nuevo socket
        //vinculado al cliente para poder atenderlo
        socket_router = accept(socket_servidor, (struct sockaddr *) &dir_router, &tam);
        while(!enter_presente) {
            //Limpia el buffer
            memset(buffer, 0, 200);
            //Se lee una cadena desde el socket cliente
            nbytes = read(socket_router, buffer, 200);
            //Se copia el buffer leido hacia la cadena
            strncat(cadena, buffer, nbytes);
            //Si el buffer contiene un ENTER se termina la lectura
            if (strstr(buffer, "\n"))
                enter_presente = 1;
        }
        //Se calcula la longitud de la cadena completa memset
        nbytes = strlen(cadena);
        //Se imprime la cadena leida desde la conexión
        printf("El router ha enviado: %s", cadena);
        //La cadena se convierte en mayúsculas
        for(int i=0; i<nbytes; i++)
            cadena[i] = toupper(cadena[i]);
        //Se devuelve un mensaje al cliente
        write(socket_router, cadena, nbytes);
        //Se cierra el socket cliente 
        close(socket_router);
    }
}