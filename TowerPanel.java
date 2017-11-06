import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TowerPanel extends JPanel {
    TowersOfHanoi towerEngine;
    Tower[] towers;
    
    int numDisks;
    int pauseTime;
    int firstTower;
  //  boolean solved;
    boolean userSolve;
    boolean firstTowerSelected;

    ArrayList solution;
    Timer animationTimer;
    
    static final int MIN_PAUSE_TIME = 50;
    static final int MAX_PAUSE_TIME = 1500;
    static final int DEFAULT_PAUSE_TIME = 900;
    static final int MIN_DISKS = 1;
    static final int MAX_DISKS = 8;
    static final int DEFAULT_NUM_DISKS = 4;
    static final int NUM_TOWERS = 3;
    
    final String TRY_AGAIN_MESSAGE = "Incorrect move\n Try Again?";
    final String SOLVED_MESSAGE = "Congratulations!\nYou solved the puzzle!";

   //-----------------------------------------------------------------
   //  Creates a new tower panel to diplay the Towers of Hanoi
   //-----------------------------------------------------------------
    public TowerPanel(int disks) 
    {
      numDisks = disks;
      pauseTime = DEFAULT_PAUSE_TIME;
      solution = new ArrayList();
      userSolve = false;
      firstTowerSelected = false;
      
      towers = new Tower[NUM_TOWERS];
      for (int i=0; i<NUM_TOWERS; i++)
          towers[i] = new Tower(numDisks);
      
      //add sized disks to first tower
      for (int i=numDisks; i>0; i--)
        towers[0].addDisk(new Disk(i));
	
      towerEngine = new TowersOfHanoi(numDisks);
	
      setBackground(Color.white);
      
      addMouseListener(new TowerSelectListener());
    }

   //-----------------------------------------------------------------
   //  Overrides JPanels getPreferredSize().
   //----------------------------------------------------------------- 
    public Dimension getPreferredSize() 
    {
      // panel width is length of largest disk * num towers + 2 width step between towers
      int panelWidth = MAX_DISKS * Disk.WIDTH_STEP * NUM_TOWERS + 2 * Disk.WIDTH_STEP + NUM_TOWERS;
      // panel height 1.2 as tall as largest towers
      int panelHeight = Disk.HEIGHT * MAX_DISKS; 
      panelHeight = Math.round(1.2f * (float)panelHeight);
      
      return new Dimension(panelWidth, panelHeight);
    }

   //-----------------------------------------------------------------
   //  Draws the Towers of Hanoi.
   //-----------------------------------------------------------------
     public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int towerSpacing = getSize().width /  NUM_TOWERS;
        
        int towerX = towerSpacing/2 - Tower.WIDTH/2;
        int towerY = getSize().height;
        
        for (int i=0; i<NUM_TOWERS; i++)
        {
            towers[i].draw(g, towerX, towerY);
            towerX += towerSpacing;
        }
    }

   //-----------------------------------------------------------------
   //  Sets the amount of time in milliseconds to pause between each
   //  animation step.  If in animation mode, resets the animation
   //  timer
   //-----------------------------------------------------------------
    public void setPauseTime(int time)
    {
        pauseTime = time;
        if (animationTimer == null)
            return;
        if (userSolve)  // no animation
            return;
        // update animation speed
        if (!solution.isEmpty() && animationTimer.isRunning()) 
        {
            stopTimer();
            animationTimer = new Timer(pauseTime, new UpdateAnimation());
            animationTimer.start();
        }
    }
    
   //-----------------------------------------------------------------
   //  Sets the number of disks and resets the puzzle.
   //-----------------------------------------------------------------   
    public void setNumDisks(int num)
    {
        numDisks = num;
        stopTimer();            // stop any animation
        userSolve = false;      // stop any user solution
        resetTowers();
    }
   
   //-----------------------------------------------------------------
   //  Animates the current solution.  Stops any user solution
   //  If the solution is empty, resets the towers.
   //-----------------------------------------------------------------
     public void animate()
    {
        userSolve = false; // stop user solution
        stopTimer();  // temporarily stop timer
        if (solution.isEmpty())
            resetTowers();
                                // start new timer
        animationTimer = new Timer(pauseTime, new UpdateAnimation());
        animationTimer.start();
    }
    
    
   //-----------------------------------------------------------------
   //  Allows a user to step-by-step solve the puzzle.
   //  Stops any animation and starts user solution mode.
   //  If solution is empty, resets the towers.
   //-----------------------------------------------------------------
    public void userSolve()
    {
        stopTimer();  // stop animation
        if (solution.isEmpty())
            resetTowers(); // start over
        userSolve = true;  // check for mouse motion
    }
    
   //-----------------------------------------------------------------
   //  Resets the towers to current settings.
   //  Creates new towers, tower engine, solution, and 
   //  redraws the screen.
   //-----------------------------------------------------------------
    public void resetTowers() 
    {
      towers = new Tower[NUM_TOWERS];
      for (int i=0; i<NUM_TOWERS; i++)
          towers[i] = new Tower(numDisks);
      
      for (int i=numDisks; i>0; i--)
        towers[0].addDisk(new Disk(i));
      
      towerEngine = new TowersOfHanoi(numDisks);
      solution = towerEngine.getSolution();
      repaint();
    }
    
   //-----------------------------------------------------------------
   //  Stops the animation timer.
   //-----------------------------------------------------------------
    public void stopTimer()
    {
        if (animationTimer == null)
            return;
        if (animationTimer.isRunning())
            animationTimer.stop();
    }
    
   //-----------------------------------------------------------------
   //  Resumes the animation timer, stops any user solutions
   //-----------------------------------------------------------------
    public void resumeTimer()
    {
        if (animationTimer == null)
            return;
        if (!animationTimer.isRunning())  // get new settings and restart
        {
           userSolve = false;
           animationTimer = new Timer(pauseTime, new UpdateAnimation());
           animationTimer.start();
        }
    }
    
    //********************************************************************
    //  Represents the listener class for the animation timer.
    //********************************************************************
    class UpdateAnimation implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (!solution.isEmpty()) 
            {
                Move nextMove = (Move)solution.remove(0);
                Disk d = towers[nextMove.getFrom()].removeDisk();
                towers[nextMove.getTo()].addDisk(d);
                repaint();
            }
        }
    }
    
    //********************************************************************
    //  Represents the mouse listener class for the panel.  Used in
    //  the user solve mode.
    //********************************************************************    
    class TowerSelectListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            if (!userSolve)
                return;
            if (solution.isEmpty())
                return;

            int towerSpacing = getSize().width /  NUM_TOWERS;                

            // determine tower choosen
            int selectedTower;
            int xValue = e.getX();
            if (xValue < towerSpacing)
                selectedTower = 0;
            else
                if (xValue < 2 * towerSpacing)
                    selectedTower = 1;
                else
                    selectedTower = 2;
            if (!firstTowerSelected)
            {
                firstTower = selectedTower;
                firstTowerSelected = true;
            }
            else
            {
                // determine if valid move was selected
                firstTowerSelected = false;
                
                Move currentMove = (Move)solution.get(0);
                // if currentMove correct  . . .
                if (currentMove.getFrom() == firstTower &&
                        currentMove.getTo() == selectedTower)
                {
                    solution.remove(0);
                    Disk d = towers[currentMove.getFrom()].removeDisk();
                    towers[currentMove.getTo()].addDisk(d);
                    repaint();
                }
                else // incorrect move
                {
                    int result =JOptionPane.showConfirmDialog(null, 
                                TRY_AGAIN_MESSAGE, 
                                "", 
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                    // show solution anyway?
                    if (result != JOptionPane.YES_OPTION) 
                    {
                        solution.remove(0);
                        Disk d = towers[currentMove.getFrom()].removeDisk();
                        towers[currentMove.getTo()].addDisk(d);
                        repaint();
                    }
                        
                }
            }
            // display solved message
            if (solution.isEmpty())
            {
                JOptionPane.showMessageDialog(null,
                                        SOLVED_MESSAGE,
                                        "",
                                        JOptionPane.INFORMATION_MESSAGE);
            }
            
        }
    }
}
