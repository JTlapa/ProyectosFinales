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
    int socket_router;
    struct sockaddr_in dir_router;
    unsigned short puerto_router = 9090u;
    char *direccion_router;
    struct protoent *protoent;    
    struct hostent *hostent;
    in_addr_t in_addr;
    
    direccion_router = "127.0.0.1";
    
    
    //Obtiene la configuración para el socket TCP
    protoent = getprotobyname(protocolo);

 

    //Crea el socket del dominio Internet, para comunicaciones orientadas a conexión y con el protocolo seleccionado
    socket_router = socket(AF_INET, SOCK_STREAM, protoent->p_proto);
    
    //obtenemos la dirección IP del router 
    in_addr = inet_addr(direccion_router);
    
    //Se configura la dirección en la que escucha el router
    dir_router.sin_family = AF_INET;
    dir_router.sin_addr.s_addr = in_addr;
    dir_router.sin_port = htons(puerto_router);
    
    //Se realiza la conexión del cliente al router
    if (connect(socket_router, (struct sockaddr *)&dir_router, sizeof(struct sockaddr_in)) == -1) {
        perror("Error en la conexión al router");
        exit(-1);
    }
        
        
    char buffer[200];  
    int nbytes=0;                      
                    
    //Limpia el buffer
    memset(buffer, 0, 200);
    //Se lee una cadena desde el teclado sendto
    printf("Ingrese una cadena: ");
    fgets(buffer, 200, stdin);
    
    nbytes = strlen(buffer);
    
    //Se envía la cadena al router
    write(socket_router, buffer, nbytes);
    
    //Se lee la respuesta del router
    
    nbytes = read(socket_router, buffer, 200);            
                
    //Se imprime la cadena leida desde la conexión
    printf("El router ha respondido: %s", buffer);
    
    //Se cierra el socket cliente 
    close(socket_router);

 

}