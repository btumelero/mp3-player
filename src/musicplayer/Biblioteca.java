/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Bruno
 */
public class Biblioteca {
  private static Map<String, ArrayList<Musica>> playlists;
  private ArrayList<Musica> playlistAtual;
  private final EnumGeneros[] generos;
  private boolean playlistAlterada;
  
  /**
   * verifica se é um arquivo ou pasta e chama o método addMusicaArquivo
   * @param file arquivo/pasta a ser adicionado 
   */
  public void addMusica (File file) {
    if (file.isDirectory()) {
      File[] musicasPasta = file.listFiles((File file1) -> file1.isFile());//filtra se não for arquivo
      for (File musica : musicasPasta) {
        addMusicaArquivo(musica);
      }
    } else {
      if (file.isFile()) {
        addMusicaArquivo(file);
      }
    }
  }
  
  private void addMusicaArquivo (File file) {
    if (file.getName().contains(".mp3")) {//para não repetir o código 3 vezes
      leituraMetadataID3v1(file);
    }
  }
  
  /**
   * Carrega o HashMap playlists do arquivo Biblioteca.txt
   */
  public static void carregarBiblioteca () {
    File file = new File("Biblioteca.txt");
    if (file.exists()) {
      try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
        playlists = (Map<String, ArrayList<Musica>>) objectInputStream.readObject();
      }
      catch (FileNotFoundException ex) {
        System.out.println("Não foi possível abrir o aquivo '" + file.getName() + "'");
      }
      catch (IOException e) {
        System.out.println("Erro ao ler o arquivo " + file.getName() + "'");
      }
      catch (ClassNotFoundException ex) {
        System.out.println("Não é uma musica " + file.getName() + "'");
      }
    }
  }
  
  /**
   * Cria uma playlist usando o nome e as músicas passadas por parâmetro
   * @param nome nome da nova playlist
   * @param musicas arraylist de musicas que farão parte da playlist
   */
  public void criarPlaylist (String nome, ArrayList<Musica> musicas) {
    playlists.put(nome, musicas);
  }
  
  /**
   * Retorna uma playlist. Tive que colocar como static pra poder usar na thread
   * que salva o programa quando ele é finalizado
   * @param nome playlist que será retornada
   * @return arraylist de músicas (playlist)
   */
  public static ArrayList<Musica> getPlaylist (String nome) {
    return playlists.get(nome);
  }
  
  /**
   * retorna se a playlist foi alterada ou não
   * @return true se a playlist foi alterada
   */
  public boolean getPlaylistAlterada () {
    return playlistAlterada;
  }
  
  /**
   * Retorna a playlist atual
   * @return arraylist contendo a playlist atual
   */
  public ArrayList<Musica> getPlaylistAtual () {
    return playlistAtual;
  }
  
  /**
   * Retorna o arraylist com todas as músicas
   * @return arraylist com todas as músicas
   */
  public ArrayList<Musica> getTodasAsMusicas () {
    return playlists.get("TodasAsMúsicas");
  }
  
  /**
   * Usado para o ComboBox com as playlists salvas
   * @return nome de todas as playlists
   */
  public String[] getTodasAsPlaylists () {
    ArrayList<Object> arrayListNomes = new ArrayList<>();
    playlists.keySet().forEach((nomeDaPlaylist) -> {
      arrayListNomes.add(nomeDaPlaylist);
    });
    String[] nomes = new String[arrayListNomes.size()];
    for (int i = 0; i < arrayListNomes.size(); i++) {
      nomes[i] = arrayListNomes.get(i).toString();
    }
    return nomes;
  }
  
  /**
   * Retorna verdadeiro se a playlist TodasAsMúsicas estiver vazia. Tive que colocar 
   * como static pra poder usar na thread que salva o programa quando ele é finalizado
   * @return true se TodasAsMúsicas estiver vazio
   */
  public static boolean isEmpty () {
    return getPlaylist("TodasAsMúsicas").isEmpty();
  }
  
  private void leituraMetadataID3v1 (File file) {
    File arquivo = new File("Biblioteca.txt");
    try (FileInputStream metadata = new FileInputStream(file);) {
      int tamanho = (int)file.length();
      metadata.skip(tamanho - 128);
      byte[] ultimos128 = new byte[128];
      metadata.read(ultimos128);
      String id3 = new String(ultimos128);
      String tag = id3.substring(0, 3);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(arquivo, false));
      if (tag.equals("TAG")) {
        Musica musica = new Musica();
        musica.titulo = id3.substring(3, 33);
        musica.artista = id3.substring(33, 63);
        musica.album = id3.substring(63, 93);
        musica.ano = id3.substring(93, 97);
        musica.comentario = id3.substring(97, 127);
        musica.caminho = file.getPath();//para poder tocar a música usando o FileInputStream
        if (Integer.getInteger(id3.substring(127)) != null) {
          musica.genero = generos[Integer.getInteger(id3.substring(127))];
        }
        boolean naBiblioteca = false;                       
        for (int i = 0; i < playlists.get("TodasAsMúsicas").size(); i++) {          
          if (playlists.get("TodasAsMúsicas").get(i).titulo.equals(musica.titulo)) {
            naBiblioteca = true;                            
          }
        }
        if (naBiblioteca == false) {
          playlists.get("TodasAsMúsicas").add(musica);
          System.out.println(musica.titulo + " adicionada com sucesso");
        } else {
          System.out.println(musica.titulo + " já está na biblioteca");
        }
      } else {
        System.out.println(file + " não contém informação ID3v1.");
      }
      objectOutputStream.close();
    } 
    catch (FileNotFoundException ex) {
      System.out.println("Não foi possível abrir o arquivo '" + file + "'");
    }
    catch (IOException e) {
      System.out.println("Erro ao ler o arquivo " + file + "'");
    }
  }
  
  /**
   * Ordena a biblioteca de acordo com o critério
   * @param playlist arraylist de músicas que serão ordenadas
   * @param crit critério pelo qual será organizada a biblioteca
   * @return ArrayList de musicas ordenadas
   */
  public ArrayList<Musica> ordenarBiblioteca (ArrayList<Musica> playlist, String crit) {
    int tamanho = playlist.size();
    if (playlist.isEmpty() == false) {
      crit = crit.toUpperCase();
      if (crit.equals(EnumCriterio.TITULO.toString())) {
        playlist.sort((Musica m1, Musica m2) -> m1.titulo.compareTo(m2.titulo));
        System.out.println("\n" + playlist.get(0).titulo.charAt(0) + ":\n"
                          +playlist.get(0).titulo);
        for (int i = 1; i < tamanho; i++) {
          if (playlist.get(i).titulo.charAt(0) != playlist.get(i-1).titulo.charAt(0)) {
            System.out.println("\n" + playlist.get(i).titulo.charAt(0) + ":");
          }                                          //checando a primeira letra pra ver se ela mudou
          System.out.println(playlist.get(i).titulo);//e imprimindo as músicas separadas de acordo
        }                                            //com a letra que ela começa. todas as ordenações
      } else {                                       //fazem basicamente a mesma coisa
        if (crit.equals(EnumCriterio.ALBUM.toString())) {
          playlist.sort((Musica m1, Musica m2) -> m1.album.compareTo(m2.album));
          System.out.println("\nÁlbum: " + playlist.get(0).album + "\n"
                            +playlist.get(0).titulo);
          for (int i = 1; i < tamanho; i++) {
            if (playlist.get(i-1).album.equals(playlist.get(i).album) == false) {
              System.out.println("\nÁlbum: " + playlist.get(i).album);
              System.out.println(playlist.get(i).titulo);
            }
          }
        } else {
          if (crit.equals(EnumCriterio.ANO.toString())) {
            playlist.sort((Musica m1, Musica m2) -> m1.ano.compareTo(m2.ano));
            System.out.println("\nAno: " + playlist.get(0).ano + "\n"
                              +playlist.get(0).titulo);
            for (int i = 1; i < tamanho; i++) {
              if (playlist.get(i-1).ano.equals(playlist.get(i).ano) == false) {
                System.out.println("\nAno: " + playlist.get(i).ano);
              }
              System.out.println(playlist.get(i).titulo);
            }
          } else {
            if (crit.equals(EnumCriterio.ARTISTA.toString())) {
              playlist.sort((Musica m1, Musica m2) -> m1.artista.compareTo(m2.artista));
              System.out.println("\nArtista: " + playlist.get(0).artista + "\n"
                                +playlist.get(0).titulo);
              for (int i = 1; i < tamanho; i++) {
                if (playlist.get(i-1).artista.equals(playlist.get(i).artista) == false) {
                  System.out.println("\nArtista: " + playlist.get(i).artista);
                }
                System.out.println(playlist.get(i).titulo);
              }
            } else { //gênero:
              ArrayList<Musica> musicasComGenero = new ArrayList<>();
              System.out.println("\nGênero: Nulo");          //como tinha várias músicas sem gênero,
              for (int i = 0; i < tamanho; i++) {            //tive que criar um segundo arraylist
                if (playlist.get(i).genero == null) {        //pro método sort não dar problema
                  System.out.println(playlist.get(i).titulo);//aqui eu imprimo todas sem gênero e
                } else {                                     //adiciono as que tem no outro pra ordenar
                  musicasComGenero.add(playlist.get(i));     //aí eu ordeno e imprimo abaixo
                }
              }
              if (musicasComGenero.isEmpty() == false) {
                musicasComGenero.sort((Musica m1, Musica m2) -> m1.genero.compareTo(m2.genero));
                System.out.println("\nGênero: " + musicasComGenero.get(0).genero + "\n"
                                  +musicasComGenero.get(0).titulo);
                for (int i = 1; i < tamanho; i++) {
                  if (musicasComGenero.get(i-1).genero.equals(musicasComGenero.get(i).genero) == false) {
                    System.out.println("\nGênero: " + musicasComGenero.get(i).genero);
                  }
                  System.out.println(musicasComGenero.get(i).titulo);
                }
              }
            }
          }
        }
      }
      return playlist;
    } else {
      System.out.println("\nNão há músicas na biblioteca");
    }
    return null;
  }
  
  /**
   * Pesquisa na biblioteca as musicas de acordo com o critério e com a string que se deseja encontrar
   * @param playlist playlist em que será feita a pesquisa
   * @param crit critério a ordenar a biblioteca
   * @param str string a pesquisar na biblioteca
   * @return arraylist de músicas que batem com a pesquisa
   */
  public ArrayList<Musica> pesquisarNaBiblioteca (ArrayList<Musica> playlist, String crit, String str) {
    int tamanho = playlist.size();
    ArrayList<Musica> resultados = new ArrayList<>();
    if (playlist.isEmpty() == false) {
      crit = crit.toUpperCase();
      if (crit.equals(EnumCriterio.TITULO.toString())) {
        for (int i = 0; i < tamanho; i++) {
          if (playlist.get(i).titulo.toLowerCase().contains(str.toLowerCase())) {
            resultados.add(playlist.get(i));//o if acima explica sozinho como o método funciona, basicamente
          }
        }
      } else {
        if (crit.equals(EnumCriterio.ALBUM.toString())) {
          for (int i = 0; i < tamanho; i++) {
            if (playlist.get(i).album.toLowerCase().contains(str.toLowerCase())) {
              resultados.add(playlist.get(i));
            }
          }
        } else {
          if (crit.equals(EnumCriterio.ANO.toString())) {
            for (int i = 0; i < tamanho; i++) {
              if (playlist.get(i).ano.toLowerCase().contains(str.toLowerCase())) {
                resultados.add(playlist.get(i));
              }
            }
          } else {
            if (crit.equals(EnumCriterio.ARTISTA.toString())) {
              for (int i = 0; i < tamanho; i++) {
                if (playlist.get(i).artista.toLowerCase().contains(str.toLowerCase())) {
                  resultados.add(playlist.get(i));
                }
              }
            } else {
              if (crit.equals(EnumCriterio.GENERO.toString())) {
                for (int i = 0; i < tamanho; i++) {
                  if (playlist.get(i).genero != null) {
                    if (playlist.get(i).genero.toString().toLowerCase().contains(str.toLowerCase())) {
                      resultados.add(playlist.get(i));
                    }
                  }
                }
              }
            }
          }
        }
      }
      return resultados;
    } else {
      System.out.println("\nNão há músicas na biblioteca");
    }
    return null;
  }
  
  /**
   * Remove uma playlist
   * @param nome nome da playlist que será removida
   */
  public void removerPlaylist (String nome) {
    playlists.remove(nome);
  }
  
  /**
   * Salva o HashMap playlists no arquivo Biblioteca.txt
   */
  public static void salvarBiblioteca () {
    String arquivo = "Biblioteca.txt";
    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(arquivo))) {
      objectOutputStream.writeObject(playlists);
      objectOutputStream.flush();
    } 
    catch (FileNotFoundException ex) {
      System.out.println("Não foi possível abrir o aquivo '" + arquivo + "'");
    } 
    catch (IOException ex) {
      System.out.println("Erro ao ler o arquivo " + arquivo + "'");
    }
  }

  /**
   * setta a playlist como alterada ou não
   * @param valor true ou false para definir se a playlist foi alterada ou não
   */
  public void setPlaylistAlterada (boolean valor) {
    playlistAlterada = valor;
  }  
  
  /**
   * Atualiza a playlist atual
   * @param playlistAtual arraylist contendo a nova playlist
   */
  public void setPlaylistAtual (ArrayList<Musica> playlistAtual) {
    this.playlistAtual = playlistAtual;
  }
  
  public Biblioteca() {
    this.playlistAlterada = false;
    Biblioteca.playlists = new HashMap<>();
    Biblioteca.playlists.put("TodasAsMúsicas", new ArrayList<>());
    this.generos = EnumGeneros.values();
    Biblioteca.carregarBiblioteca();
  }
}
