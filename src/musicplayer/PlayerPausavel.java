/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * Player Pausável adaptado do original fornecido pelo professor
 * @author Bruno
 */
public class PlayerPausavel {
  private EnumEstado statusDoPlayer;
  private Player player;
  private final Object playerLock;
  private final ArrayList<Musica> playlist;
  private Thread t;
  private int contador;

  /**
   * Retorna o estado atual do player
   * @return Estado do Player
   */
  public EnumEstado getEstadoDoPlayer () {
    return statusDoPlayer;
  }
  
  /**
   * Cria a thread que toca a música ou resume caso já esteja tocando
   */
  public void play() {
    synchronized (playerLock) {
      if (statusDoPlayer == EnumEstado.NAOINICIADO || statusDoPlayer == EnumEstado.PARADOPELOUSUARIO) {
        final Runnable r = () -> {
          playInterno();
        };
        t = new Thread(r);
        t.setPriority(Thread.MAX_PRIORITY);
        statusDoPlayer = EnumEstado.TOCANDO;
        t.start();
      } else {
        if (statusDoPlayer == EnumEstado.PAUSADO) {
          resume();
        }
      }
    }
  }

  /**
   * Pausa o player
   * @return true se o player pausou
   */
  public boolean pause() {
    synchronized (playerLock) {
      if (statusDoPlayer == EnumEstado.TOCANDO) {
        statusDoPlayer = EnumEstado.PAUSADO;
      }
      return statusDoPlayer == EnumEstado.PAUSADO;
    }
  }

  /**
   * Continua a execução da música
   * @return true se o player resumiu
   */
  public boolean resume() {
    synchronized (playerLock) {
      if (statusDoPlayer == EnumEstado.PAUSADO) {
        statusDoPlayer = EnumEstado.TOCANDO;
        playerLock.notifyAll();
      }
      return statusDoPlayer == EnumEstado.TOCANDO;
    }
  }

  /**
   * Para a execução da música
   */
  public void stop() {
    synchronized (playerLock) {
      statusDoPlayer = EnumEstado.PARADOPELOUSUARIO;
      playerLock.notifyAll();
    }
  }

  private void playInterno() {
    while (statusDoPlayer != EnumEstado.TERMINOUDETOCAR && statusDoPlayer != EnumEstado.PARADOPELOUSUARIO) {
      try {
        if (player.play(1) == false) {//para se não conseguir tocar 1 frame da música
          break;
        }
      } 
      catch (DecoderException ex) {
        System.out.println("Erro decoder");
      }
      catch (BitstreamException ex) {
        System.out.println("Erro bitstream 102 - A problem occurred reading from the stream.");
        break;
      } 
      catch (JavaLayerException ex) { 
        System.out.println("Erro"); 
      }
      synchronized (playerLock) {
        while (statusDoPlayer == EnumEstado.PAUSADO) {
          try {
            playerLock.wait();
          } 
          catch (final InterruptedException e) {
            break;
          }
        }
      }
    }
    close();
  }

  /**
   * quando a execução de uma música é terminada, seja pelo usuário ou pelo player, esse método é
   * chamado. Se a música terminou de tocar, a próxima é executada, senão, a execução é interrompida
   * até o usuário dar play novamente ou fechar o programa
   */
  public void close() {
    synchronized (playerLock) {
      if (statusDoPlayer != EnumEstado.PARADOPELOUSUARIO) {
        statusDoPlayer = EnumEstado.TERMINOUDETOCAR;
      }
    }
    try {
      if (statusDoPlayer == EnumEstado.TERMINOUDETOCAR) {
        if (contador + 1 == playlist.size()) {
          contador = 0;
        } else {
          contador++;
        }
        player = new Player(new FileInputStream(playlist.get(contador).caminho));
        statusDoPlayer = EnumEstado.NAOINICIADO;
        play();
      } else {
        if (statusDoPlayer == EnumEstado.PARADOPELOUSUARIO) {
          player = new Player(new FileInputStream(playlist.get(contador).caminho));
        }
      }
    } 
    catch (FileNotFoundException ex) {
      System.out.println("File not found");
    }
    catch (final Exception e) {
      //ignore, we are terminating anyway
    }
  }
  
  /**
   * Avança para a próxima música
   */
  public void proximaMusica () {
    statusDoPlayer = EnumEstado.TERMINOUDETOCAR;
    
  }
  
  /**
   * Retorna para a música anterior
   */
  public void voltarMusica () {
    //só é preciso checar valores maiores que dois porque ao settar como terminou de tocar o valor do
    //contador é checado e incrementado: se a playlist tiver uma música só ele vai tentar incrementar
    //e ver que atingiu o fim da playlist e vai settar o contador para zero. se a playlist tiver duas 
    //não é necessário fazer nada porque a música anterior e a seguinte serão a mesma e ele vai passar
    //para a outra normalmente
    if (playlist.size() >= 3) {
      if (contador == 0) {
        contador = playlist.size() - 2;//setar como terminou de tocar faz com que o contador 
      } else {                            //incremente, por isso -2
        if (contador == 1) {
          contador = playlist.size() - 1;
        } else {
          contador = contador - 2;
        }
      }
    }
    statusDoPlayer = EnumEstado.TERMINOUDETOCAR;
  }

  /**
   * 
   * @param playlist arraylist de InputStream das músicas que serão tocadas
   * @throws JavaLayerException
   */
  public PlayerPausavel(ArrayList<Musica> playlist) throws JavaLayerException {
    this.contador = 0;
    this.playerLock = new Object();
    this.statusDoPlayer = EnumEstado.NAOINICIADO;
    this.playlist = playlist;
    try {
      this.player = new Player(new FileInputStream(playlist.get(contador).caminho));
    } 
    catch (FileNotFoundException ex) {
      System.out.println("File not found");
    }
  }
}