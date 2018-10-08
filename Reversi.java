/*
 * @author Anna Burkhart - alb171
 * @param EECS 132 Project 4 - Reversi Game
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/* This is the Reversi class which uses JFrame and Action Listener to create the Reversi game
 */
public class Reversi extends JFrame implements ActionListener{
  /* @param height is the height of the board 
   */ 
  private JFrame frame;
  /* @param board is the grid put on the JFrame object 
   */
  private JPanel board;
  /* @param toolbar is the toolbar above the Reversi game with the dropdown options
   */ 
  private JMenuBar toolbar;
  /* @param the JMenu field contains the dropdown tabs on the toolbar 
   */ 
  private JMenu newGame, player1ColorMenu, player2ColorMenu;
  /* @param the JMenuItem field contains the options underneath each dropdown tab
   */ 
  private JMenuItem[] boardSizeOptions, player1ColorOptions, player2ColorOptions;
  /* @param the JButton field contains the location of the button
   */ 
  private JButton squares[][];
  /* @param isPlayer2Turn contains the boolean of whether or not it is Player 2's turn
   */ 
  private boolean isPlayer2Turn = true;
  /* @param Color contains the color Players 1 and 2 select from the toolbar 
   */ 
  private Color player1Color, player2Color;
  
  /* An array that gives the color options for each player to select from in the toolbar menu 
   */
  private static Color[] playerColorOptions = new Color[]{
    Color.BLACK,
    Color.BLUE,
    Color.GREEN,
    Color.ORANGE,
    Color.PINK,
    Color.RED,
    Color.WHITE
  };
  
  /* An array that contains all the names of the color options for each player 
   */ 
  private static String[] playerColorOptionNames = new String[]{
    "Black",
      "Blue",
      "Green",
      "Orange",
      "Pink",
      "Red",
      "White"
  };
  
  /* Constructor for the Reversi 8x8 grid
   */ 
  public Reversi() {
    this(8,8);
  }
  
  /* Constructor for the Reversi grid where boardsize is put in 
   */ 
  public Reversi(int boardsize) {
    this(boardsize,boardsize);
  }
  
  /* Constructor for Reversi grid where height and width can be set independently.
   * @param player1Color sets the default color for player 1 
   * @param player2Color sets the default color for player 2
   * @param middleFirstX, middleFirstY put the initial Reversi squares in the middle of the board in the default colors
   */ 
  public Reversi(int height, int width) {
    board = new JPanel(new GridLayout(width, height));
    frame = new JFrame();
    player1Color= Color.BLACK;
    player2Color= Color.WHITE;
    int middleFirstX = (int)Math.floor(width/2)-1;
    int middleFirstY = (int)Math.floor(height/2)-1;
    squares = new JButton[width][height];
    /* This for loop goes through the four buttons in the center of the grid and changes them to the correct default
     * colors
     */ 
       for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                squares[i][j] = new JButton();
                if (i == middleFirstX && j == middleFirstY ||
                    i == middleFirstX + 1 && j == middleFirstY + 1){
                  /* Makes center buttons white
                   */ 
                  turnToPlayer2Color(squares[i][j]);
                }
                if (i == middleFirstX + 1 && j == middleFirstY ||
                  i == middleFirstX && j == middleFirstY + 1){
                  /* Makes center buttons black
                   */ 
                  turnToPlayer1Color(squares[i][j]);
                }
                squares[i][j].addActionListener(this);
                board.add(squares[i][j]);
            }
       }
    frame.setLayout(new BorderLayout());
    frame.getContentPane().add(board, BorderLayout.CENTER);
    frame.setSize(600,600);
    changeTurn(false);
    createNewMenuBar();
    frame.setVisible(true);
    /* @exception makes an exception to the way the game looks if played on an Apple computer 
     */ 
      try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch (Exception e) {
    } 
  }
  
  /* Contains the board size options displayed in the toolbar to make it easier for players to customize the grid sizes
   * @param item contains the size of the board written as "# x # board"
   */ 
  public void createBoardSizeOptions() {
    boardSizeOptions = new JMenuItem[13-3];
    for (int i = 3; i < 13; i++) {
      JMenuItem item = new JMenuItem(i + "x" + i + " board");
      item.addActionListener(this);
      boardSizeOptions[i-3] = item;
      newGame.add(item);
    }
  }
  
  /* Creates the color options so that the players can select their own colors
   */ 
  public void createColorOptions(JMenu menu, JMenuItem[] options) {
    for (int i = 0; i < playerColorOptions.length; i++) {
      JMenuItem item = new JMenuItem(playerColorOptionNames[i]);
      item.addActionListener(this);
      options[i] = item;
      menu.add(item);
    }
  }
  
  /* Creates the toolbar at the top of the Reversi game
   */ 
  public void createNewMenuBar(){
    toolbar = new JMenuBar();
    /* @param newGame is the button that allows a user to start a new game
     */
    newGame = new JMenu("New Game");
    createBoardSizeOptions();
    toolbar.add(newGame);
    /* @param player1ColorMenu is the tab with player 1's color options
     * @param player1ColorOptions contains all of the colors player 1 can select
     */ 
    player1ColorMenu = new JMenu("Player 1 Color");
    player1ColorOptions = new JMenuItem[playerColorOptions.length];
    createColorOptions(player1ColorMenu, player1ColorOptions);
    toolbar.add(player1ColorMenu);
    /* @param player2ColorMenu is the tab with player 2's color options
     * @param player2ColorOptions contains all of the colors player 2 can select
     */ 
    player2ColorMenu = new JMenu("Player 2 Color");
    player2ColorOptions = new JMenuItem[playerColorOptions.length];
    createColorOptions(player2ColorMenu, player2ColorOptions);
    toolbar.add(player2ColorMenu);
    frame.setJMenuBar(toolbar);
  }
  
  /* Gets the position of the button 
   * @return should return the coordinates using i and j
   * @return returns null in button position is invalid
   * @return pieces at the end which contains the amount of pieces need to be flipped
   */ 
  public int[] getButtonPosition(JButton n){
    if (n == null){
      return null;
    }
    for (int i = 0; i<squares.length; i++){
      for (int j = 0; j<squares[i].length; j++){
        JButton b = squares[i][j];
        if (b == n){
          return new int[]{i,j};
        }
      }
    }
    return null;
  }
  
  /* Creates a linked list that keeps track of the pieces that need to be flipped when a move is made
   */ 
  public LinkedList<JButton> getFlippedPieces(int x, int y){
    LinkedList<JButton> pieces = new LinkedList<JButton>();
    /* Runs through adjacent buttons to the button clicked
     */ 
    for (int i = -1; i < 2; i++){
      for (int j = -1; j < 2; j++){
        /* Button clicked does not qualify as adjacent to the button clicked so loop continues 
         */ 
        if (i ==0 && j==0){
          continue;
        }
        /* Checks to see if the buttons are on the grid and can be clicked 
         */ 
        if (x+i < 0 || x+i >= squares.length || y+j < 0 || y+j >= squares[x+i].length){
          continue;
        }
        int count = 1;
        JButton b = squares[x+i][y+j];
        /* Makes a seperate linked list that only records the test flipped pieces and only adds the test flipped 
         * pieces to the actual flipped pieces linked list if it is valid 
         */ 
        LinkedList<JButton> testPieces = new LinkedList<JButton>();
        /* Checks to see if adjacent piece is opposite color of the player going to see if the move is valid
         */ 
        while (b != null &&
               (b.getBackground() == player2Color && !isPlayer2Turn || 
               b.getBackground() == player1Color && isPlayer2Turn)){
          testPieces.add(b);
          count++;
          /* Checks the next button in the line to see if it will also be the same color if it is valid
           */ 
          if (x+i*count < 0 || x+i*count >= squares.length ||
              y+j*count < 0 || y+j*count >= squares[x+i*count].length) {
            b = null;
          }
          else {
            b = squares[x+i*count][y+j*count];
          }
        }
        /* Checks to see if the last button in the line is the same color as the player going in order to verify
         * the line and move is valid
         */ 
        if (b != null && 
            (b.getBackground() == player2Color && isPlayer2Turn || 
            b.getBackground() == player1Color && !isPlayer2Turn)){
          pieces.addAll(testPieces);
        }
      }
    }
    return pieces;
  }
  
  /* Checks if the move is valid by seeing if the button is not clicked, on the board, and then turns the color of the
   * button accordingly 
   * @param current is the current button being clicked
   */ 
  public boolean isValidMove(int x, int y){
    JButton current = squares[x][y];
    if (current.getBackground() == player1Color || current.getBackground() == player2Color){
      return false;
    }
    /* If flippedPieces method confirms the move is valid by being not equal to 0, the button can be turned a different
     * color
     */ 
    if (getFlippedPieces(x, y).size() == 0){
      return false;
    }
    return true;
  }
  
  /* Turns button color of Player 2 
   */ 
  public void turnToPlayer2Color(JButton a){
    a.setBackground(player2Color);
  }
  
  /* Turns button color of Player 1
   */ 
  public void turnToPlayer1Color(JButton c){
    c.setBackground(player1Color);
  }
  
  /* Verifies the correct player's turn is being taken
   * @param color is the color of the player whose turn it is
   */ 
  public void takeTurn(int x, int y){
    JButton b = squares[x][y];
    LinkedList<JButton> pieces = getFlippedPieces(x, y);
    Color color;
    /* Only change color to the color of the player whose turn it is
     */ 
    if (isPlayer2Turn){
    color = player2Color;
    }
    else{
      color = player1Color;
    }
    b.setBackground(color);
    for (int i = 0; i < pieces.size(); i++){
      pieces.get(i).setBackground(color);
    }
  }
  
  /* Gets the score of the players by counting their squares to display on toolbar 
   * @param p1Score is score of player 1
   * @param p2Score is score of player 2
   * @return returns scores of players 1 and 2
   */ 
  public int[] getPlayerScores() {
    int p1Score = 0;
    int p2Score = 0;
    /* Counts the score of Player 1 based on how many squares are their color
     */ 
    for (int i = 0; i < squares.length; i++) {
      for (int j = 0; j < squares[i].length; j++) {
        JButton b = squares[i][j];
        if (b.getBackground() == player1Color) {
          p1Score++;
        }
        /* Counts the score of Player 1 based on how many squares are their color
         */ 
        if (b.getBackground() == player2Color) {
          p2Score++;
        }
      }
    }
    return new int[] { p1Score, p2Score };
  }
  
  /* Changes the turn of the players after a player has taken their turn
   * @param isPlayer2Turn determines if it is Player 2's turn to play 
   * @param aMoveExists determines whether the player has a move to make and can make their turn
   */ 
  public void changeTurn(boolean noTurnForOtherPlayer){
    int[] scores = getPlayerScores();
    isPlayer2Turn = !isPlayer2Turn;
    boolean aMoveExists = false;
    /* Checks if there is a valid move for the player to make 
     * @return returns a boolean on whether a move exists
     */ 
    for (int i = 0; i < squares.length; i++) {
      for (int j = 0; j < squares[i].length; j++) {
        if (isValidMove(i, j)) {
          aMoveExists = true;
        }
      }
    }
    /* Determines player 1 has won the game based on a higher score and no more moves
     */ 
    if (!aMoveExists) {
      if (noTurnForOtherPlayer) {
        int winner = 0;
        if (scores[0] > scores[1]) {
          winner = 1;
        }
        /* Determines player 2 has won based on a higher score and no more moves
         */ 
        if (scores[0] < scores[1]) {
          winner = 2;
        }
        /* Displays the winner in a warning message
         */ 
        if (winner > 0) {
          JOptionPane.showMessageDialog(frame, "Player " + winner + " has won!", 
                                        "Winner!", JOptionPane.WARNING_MESSAGE);
        }
        /* If no winner or equal scores, the game is a tie
         */ 
        else {
          JOptionPane.showMessageDialog(frame, "Tie!", 
                                        "Tie!", JOptionPane.WARNING_MESSAGE);
        }
        /* Closes the finished game
         */ 
        new Reversi(squares.length, squares[0].length);
        closeWindow();
      }
      /* Changes the move to the other player when there is no move to make
       */ 
      else {
        JOptionPane.showMessageDialog(frame, "No move available, changing to other player.",
                                      "No Moves", JOptionPane.WARNING_MESSAGE);
        changeTurn(true);
        return;
      }
    }
    int playerTurn = 1;
    if (isPlayer2Turn){
      playerTurn = 2;
    }
    /* Adds the scores to the top bar so players can see their scores throughout the game
     */ 
    frame.setTitle("Player " + playerTurn + " Turn - Player 1: " + scores[0] + " Player 2: " + scores[1] + " - Reversi");
  }
  
  /* Updates the players colors as they select them 
   * @param p1Color is the color Player 1 selects 
   * @param p2Color is the color Player 2 selects
   */ 
  public void updateColors(Color p1Color, Color p2Color) {
    if (p1Color == p2Color){
      return;
    }
    /* Gets and sets the background for player 1's pieces
     */ 
     for (int i = 0; i < squares.length; i++) {
      for (int j = 0; j < squares[i].length; j++) {
        JButton b = squares[i][j];
        if (b.getBackground() == player1Color){
          b.setBackground(p1Color);
        }
        /* Gets and sets the background for player 2's pieces
         */ 
         if (b.getBackground() == player2Color){
          b.setBackground(p2Color);
         }
      }
     }
     /* Updates the color
      */ 
     player1Color = p1Color;
     player2Color = p2Color;
  }
  
  /* Method handleSquareClick determines whether the turn should be changed based on the click
   */
  public void handleSquareClick(JButton b){
    int[] position = getButtonPosition(b);
    if (position != null && !isValidMove(position[0], position[1])){
      return;
    }
    takeTurn(position[0], position[1]);
    changeTurn(false);
  }
  
  /* Gives the board size options for the user
   * @return -1 since the return is type int if b is null
   */ 
  public int getBoardSizeOption(JMenuItem b){
    if (b == null){
      return -1;
    }
    /* Gives the int board lengths in the dropdown options 
     */ 
    for (int i = 0; i < boardSizeOptions.length; i++){
      if (boardSizeOptions[i] == b){
        return i + 3;
      }
    }
    return -1;
  }
  
  /* Gets the player color options for player 1 
   * @return null if the item b is null
   */ 
  public Color getPlayer1ColorOption(JMenuItem b){
    if (b == null){
      return null;
    }
    /* @return returns the color options
     */ 
    for (int i = 0; i < player1ColorOptions.length; i++){
      if (player1ColorOptions[i] == b){
        return playerColorOptions[i];
      }
    }
    return null;
  }
  
  /* Gets the player color options for player 2 
   * @return null if the item b is null
   */ 
   public Color getPlayer2ColorOption(JMenuItem b){
    if (b == null){
      return null;
    }
    for (int i = 0; i < player2ColorOptions.length; i++){
      if (player2ColorOptions[i] == b){
        return playerColorOptions[i];
      }
    }
    return null;
  }
   
  /* Closes the window 
   * @param dispatchEvent contains the window event which closes the window
   * @param WindowEvent gives the command to close the window
   */ 
  public void closeWindow() {
    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
  }
  
  /* ActionPerformed performs the handleSqaureClick method if there is a button position
   * @param e.getSource gives the source of the button clicked
   */ 
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JButton) {
      JButton b = (JButton) e.getSource();
      if (getButtonPosition(b) != null){
        handleSquareClick(b);
      }
    }
    /* Changes the game based on what is clicked from the dropdown toolbar menu
     */ 
    if (e.getSource() instanceof JMenuItem) {
      JMenuItem b = (JMenuItem) e.getSource();
      int boardSizeOption = getBoardSizeOption(b);
      Color player1ColorOption = getPlayer1ColorOption(b);
      Color player2ColorOption = getPlayer2ColorOption(b);
      /* Closes the Reversi game window when the board Size option is -1
       */ 
      if (boardSizeOption != -1) {
        new Reversi(boardSizeOption);
        closeWindow();
      }
      /* Updates colors for players
       */ 
      if (player1ColorOption != null){
        updateColors(player1ColorOption, player2Color);
      }
       if (player2ColorOption != null){
        updateColors(player1Color, player2ColorOption);
       }
    }
  }
  
  /* Creates the new Reversi game in the main method
   */ 
  public static void main(String[] args){
    new Reversi();
  }
}