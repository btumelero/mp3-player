/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.Serializable;

/**
 *
 * @author Bruno
 */
public class Musica implements Serializable {
  public String titulo, album, ano, artista, comentario, caminho;
  public EnumGeneros genero;
}
