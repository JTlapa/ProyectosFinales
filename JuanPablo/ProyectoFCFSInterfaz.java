import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class Proceso {
    private int tiempoEjecucion;
    private int memoria;

    public Proceso(int te, int m) {
        this.tiempoEjecucion = te;
        this.memoria = m;
    }

    public int getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public int getMemoria() {
        return memoria;
    }
}

class CrearHilo extends Thread {
    private final Queue<Proceso> colaEspera;
    private final int CapacidadMemoria;
    private final DefaultListModel<String> listModel;

    public CrearHilo(Queue<Proceso> colaEspera, DefaultListModel<String> listModel, int CapacidadMemoria) {
        this.colaEspera = colaEspera;
        this.listModel = listModel;
        this.CapacidadMemoria = CapacidadMemoria;
    }

    public void run() {
        Random random = new Random();
        int i = 1;

        while (true) {
            int te = random.nextInt(10) + 1;
            int me = random.nextInt(10) + 1;

            // Verifica si el nuevo proceso excede la capacidad de memoria
            synchronized (colaEspera) {
                int memoriaActual = colaEspera.stream().mapToInt(Proceso::getMemoria).sum();
                if (memoriaActual + me <= CapacidadMemoria) {
                    Proceso nuevoProceso = new Proceso(te, me);
                    System.out.println("Nuevo proceso: " + i + " - Tiempo de ejecución: " + te + " - Memoria: " + me);

                    // Añade el nuevo proceso a la cola
                    colaEspera.offer(nuevoProceso);
                    listModel.addElement("Nuevo proceso: " + i + " - Memoria: " + me);

                    colaEspera.notify(); // Notifica al hilo FCFS que hay un nuevo proceso
                }
            }

            try {
                // Tiempo aleatorio de espera antes de crear el próximo proceso
                Thread.sleep(random.nextInt(500) + 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}

class FCFS extends Thread {
    private final Queue<Proceso> colaEspera;
    private final DefaultListModel<String> listModel;

    public FCFS(Queue<Proceso> colaEspera, DefaultListModel<String> listModel) {
        this.colaEspera = colaEspera;
        this.listModel = listModel;
    }

    public void run() {
        while (true) {
            Proceso procesoActual;
            // Espera a que haya un proceso en la cola
            synchronized (colaEspera) {
                while (colaEspera.isEmpty()) {
                    try {
                        colaEspera.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                procesoActual = colaEspera.poll();
                listModel.remove(0);
            }

            System.out.println("Atendiendo proceso - Tiempo de ejecución: " +
                    procesoActual.getTiempoEjecucion() + " - Memoria requerida: " + procesoActual.getMemoria());

            // Simula la ejecución del proceso
            try {
                Thread.sleep((procesoActual.getTiempoEjecucion() * 1000) / 2); // Multiplicado por 1000 para convertir a milisegundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class ProyectoFCFSInterfaz {
    public static final int CapacidadMemoria = 50;

    public static void main(String[] args) {
        Queue<Proceso> colaEspera = new LinkedList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        CrearHilo hiloCrear = new CrearHilo(colaEspera, listModel, CapacidadMemoria);
        FCFS fcfs = new FCFS(colaEspera, listModel);

        // Crear y configurar la ventana
        JFrame frame = new JFrame("Simulador de Procesos FCFS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Crear y configurar la lista
        JList<String> processList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(processList);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Crear botón para iniciar el sistema
        JButton startButton = new JButton("Iniciar Sistema");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hiloCrear.start();
                fcfs.start();
            }
        });
        frame.getContentPane().add(startButton, BorderLayout.SOUTH);

        // Mostrar la ventana
        frame.setVisible(true);
    }
}