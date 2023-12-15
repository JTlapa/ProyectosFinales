import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.*;
import java.awt.*;

/*
 * FIRST COME FIRST SERVED (FCFS)
 * Los procesos son ejecutados en el orden que llegan a la cola de procesos listos.
 * La implementaciOn es facil a traves de una cola FIFO.
 */

public class Principal{
	public static void main(String args[]) {
        int tiempo = 100;
        int memoria = 280;
        
        Ventana mv = new Ventana();

        Procesos procesos= new Procesos();
        SistemaOperativo SO = new SistemaOperativo(memoria,mv);
        

        HiloCrearProcesos creadorProcesos = new HiloCrearProcesos(procesos,SO,tiempo);
        HiloSistemaOperativo sistemaOpe = new HiloSistemaOperativo(procesos,SO,mv);

        

        creadorProcesos.start();
        sistemaOpe.start();
        
	}
}

class HiloCrearProcesos extends Thread {
    private Procesos proces;
    private SistemaOperativo so;
    private int escala;
	public HiloCrearProcesos(Procesos p, SistemaOperativo sisO, int esc) {
        proces=p;
        so = sisO;
        escala = esc;
    }
	@Override
	public void run() {
        while(true){
            try{
                proces.addProceso(so, escala);
                Thread.sleep(50);
                
                //System.out.println(proces.getProceso());
            }
            catch(Exception e){

            }
            
        }
	}
}

class HiloSistemaOperativo extends Thread {
    private Procesos proces;
    private SistemaOperativo so=null;
    private Proceso p;
    private Ventana mv;

	public HiloSistemaOperativo(Procesos p, SistemaOperativo sisO, Ventana _mv) {
        this.proces=p;
        so = sisO;
        mv = _mv;
    }
	@Override
	public void run() {
        
        while(true){
            mv.actualizar();
            try{
                Thread.sleep(20);
                //if(proces.getTam()>0){
                    p = proces.getProceso();
                    //System.out.println(p);
                    if(so.cabeProceso(p)){
                        mv.agregarBloque(p.getEspacio());
                        p.start();
                        proces.deleteProceso();
                    }
                    else{
                        Thread.sleep(100);
                    }
                    
                //}
            }
            catch(Exception e){

            }
            
        }
	}
}


class Proceso extends Thread{
    private int tiempo;
    private int espacio;
    private SistemaOperativo so;
    private int escala;

    public Proceso(int ti, int es,SistemaOperativo sisO, int esc){
        tiempo = ti;
        espacio = es;
        so = sisO;
        escala = esc;
    }
    public int getTiempo(){
        return tiempo;
    }
    public int getEspacio(){
        return espacio;
    }
    public String toString(){
        return "\nTimpo: "+Integer.toString(tiempo)+"\nEspacio: "+Integer.toString(espacio);
    }
    @Override
    public void run(){
        try{
            so.usar(this);
            
            Thread.sleep(tiempo*escala);
            so.dejarDeUsar(this);
        }catch(Exception e){

        }
    }
}
class Procesos{
    private Queue<Proceso> cola;
    private Semaphore mutex1;
    private Random rand;
    private Proceso p1;

    public Procesos(){
        cola = new LinkedList<>();
        mutex1 = new Semaphore(1);
        rand = new Random();
    }
    public void addProceso(SistemaOperativo sisO, int esc) throws InterruptedException{
        mutex1.acquire();

        int ti=rand.nextInt(100)+1;   //1-100
        int esp = rand.nextInt(71)+30;  //30-100
        
        p1 = new Proceso(ti, esp, sisO, esc);
        //System.out.println("hola "+p1);
        cola.add(p1);
        mutex1.release();
    }
    public Proceso getProceso() throws InterruptedException{
        mutex1.acquire();
        Proceso p1 = cola.peek();
        mutex1.release();
        return p1;
    }
    public void deleteProceso() throws InterruptedException{
        mutex1.acquire();
        cola.remove();
        mutex1.release();
    }
    public int getTam(){
        return cola.size();
    }
}

class SistemaOperativo{
    private int memoria;
    private int memoriaC;
    private Semaphore mutex;
    private Ventana mv;

    public SistemaOperativo( int me, Ventana _mv){
        memoria = me;
        memoriaC = me;
        mutex = new Semaphore(1);
        mv=_mv;
    }
    public void usar(Proceso p) throws InterruptedException{
        //mv.agregarBloque(p.getEspacio());
        mutex.acquire();
        mv.cambiarColor(p);
    }
    public void dejarDeUsar(Proceso p) throws InterruptedException{
        
        memoria += p.getEspacio();
        mv.eliminar();
        mutex.release();
    }
    public boolean cabeProceso(Proceso p) throws InterruptedException{
        //mutex.acquire();
        boolean band=false;
        if(memoria>=p.getEspacio()){
            //mv.agregarBloque(p.getEspacio());
            memoria -= p.getEspacio();
            mv.actualizaMemoria(memoria,memoriaC);
            //System.out.println(memoria);
            band = true;
        }
        //mutex.release();
        return band;
    }
}

class Ventana extends JFrame{
    private JPanel panel;
    private JPanel bloque;
    private Queue<JPanel> bloques;
    private JPanel leyenda;
    private JLabel leyendaProceso, leyendaMemoria;

    public Ventana(){
        bloques = new LinkedList();

        this.setTitle("Simulador de Cola de Procesador");
        this.setSize(600, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        leyendaProceso=new JLabel();
        leyendaMemoria = new JLabel();

        

        leyenda = new JPanel();
        leyenda.setLayout(new BorderLayout());
        leyenda.add(leyendaProceso, BorderLayout.NORTH);
        leyenda.add(leyendaMemoria, BorderLayout.SOUTH);
        
        this.add(panel,BorderLayout.NORTH);
        this.add(leyenda, BorderLayout.CENTER);

        

        this.setVisible(true);
    }

    public void agregarBloque(int me){
        bloque = new JPanel();
        bloque.add(new JLabel(Integer.toString(me)));
        bloque.setBackground(Color.orange);
        bloque.setPreferredSize(new Dimension(me, 50));
        panel.add(bloque);
        bloques.add(bloque);
    }

    public void cambiarColor(Proceso p){
        bloque = bloques.peek();
        bloque.setBackground(Color.green);

        leyendaProceso.setText("<html>Proceso actual:<br>Tiempo: "+p.getTiempo()+"<br>Espacio: "+p.getEspacio()+" </html>");
        }
    public void actualizaMemoria(int memo, int memoC){
        leyendaMemoria.setText("<html>Memoria: <br>Disponible: "+Integer.toString(memo)+"<br>Ocupada: "+Integer.toString(memoC-memo)+"</html>");
    
    }
    public void eliminar(){
        panel.remove(bloques.peek());
        bloques.remove();
    }
    public void actualizar(){
        this.revalidate();
        this.repaint();
    }

}