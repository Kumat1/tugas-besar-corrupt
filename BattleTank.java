package Tank;

import java.awt.*;
import java.applet.Applet;
import java.applet.AudioClip;

public class BattleTank extends Applet implements Runnable
{
  final	double	bulletspeed = 4;
  final double	tankspeed = 1;
  final double	pi=(double)3.141592653589793238462643383279502884197;
  final int	screendelay = 350;
  final int	rotsteps = 80;
  final int	bulletlife=70;
  final int	polycoords = 16; // should match the number of array entries below
  final double	tankpoly_x[] = { -10, -6, -6, -2, -2, 2, 2, 6, 6, 10, 10, 6, 6, -6, -6, -10 };
  final double	tankpoly_y[] = { -8, -8, -4, -4, -10, -10, -4, -4, -8, -8, 8, 8, 4, 4, 8, 8 };
  final double	collisionrange = 7;
  final double	blockers[] = {	0,24, 400, 30,		// Upper wall
				0,24, 6, 336,
				394,24, 400,336,
				0,330, 400,336,
                100,177, 300,183,
				100,150, 106,210,
				294,150, 300,210,
				150,70, 250, 76,
				150,284, 250,290,
				-1,-1,-1,-1 };
  Dimension	d;
  Font 		largefont = new Font("Helvetica", Font.BOLD, 24);
  Font		smallfont = new Font("Helvetica", Font.BOLD, 14);
  FontMetrics	fmsmall, fmlarge;
  Graphics	goff;
  Image		ii;
  Thread	thethread;
  boolean	ingame=false;
  Color		tank1color = new Color(255,192,128);
  Color		tank2color = new Color(192,255,128);
  Color		backgnd = new Color(16,24,64);
  double	tank1x, tank1y, tank2x, tank2y;
  double	bullet1x, bullet1y, bullet2x, bullet2y;
  double	bullet1dx, bullet1dy, bullet2dx, bullet2dy;
  boolean	steerablebullets=true;
  double	tank1angle, tank2angle, bullet1angle, bullet2angle;
  double	tank1dangle, tank2dangle;
  boolean	bullet1fired, bullet2fired;
  double	tank1drive, tank2drive;
  boolean	showtitle;
  boolean	tank1dying, tank2dying;
  int		player1score, player2score;
  int		bullet1count,bullet2count;
  int 		count = screendelay;
  int[]		xcoord;
  int[]		ycoord;
  int		dyingcount;
  AudioClip     shot;
  AudioClip     explosion;
  
  public String getAppletInfo()
  {
    return("Battle Tank -  by Dhean and Kevin");
  }

  public void init()
  {
    Graphics g;
    int i;
    d = size();
    g=getGraphics();
    g.setFont(smallfont);
    fmsmall = g.getFontMetrics();
    g.setFont(largefont);
    fmlarge = g.getFontMetrics();
    xcoord = new int[polycoords];
    ycoord = new int[polycoords];
    shot = getAudioClip(getDocumentBase(),"shot.wav");
    explosion = getAudioClip(getDocumentBase(),"explosion.wav");
    GameInit();
  }

  public void GameInit()
  { 
    tank1x=50;
    tank2x=350;
    tank1y=180;
    tank2y=180;
    tank1angle=0;
    tank2angle=pi;
    bullet1fired = false;
    bullet2fired = false;
    tank1drive = 0;
    tank2drive = 0;
    showtitle = true;
    tank1dying=false;
    tank2dying=false;
    dyingcount=30;
  }


  public boolean keyDown(Event e, int key)
  {
    if (ingame)
    {
      if (key=='d' || key=='D')
        tank1dangle=-pi/(rotsteps/2);
      if (key=='a' || key=='A')
        tank1dangle=+pi/(rotsteps/2);
      if (key=='w' || key=='W')
        tank1drive=1.0;
      if (key=='s' || key=='S')
        tank1drive=-1.0;
      if ((key=='f' || key=='F') && !bullet1fired)
      {
        bullet1fired=true;
        bullet1x=tank1x;
        bullet1y=tank1y; 
        bullet1dx=bulletspeed*Math.cos(tank1angle);
        bullet1dy=-bulletspeed*Math.sin(tank1angle);
        bullet1count=bulletlife;
        shot.play();
      }
      if (key=='6' || key==Event.RIGHT)
        tank2dangle=-pi/(rotsteps/2);
      if (key=='4' || key==Event.LEFT)
        tank2dangle=+pi/(rotsteps/2);
      if (key=='8' || key==Event.UP)
        tank2drive=1.0;
      if (key=='2' || key==Event.DOWN)
        tank2drive=-1.0;
      if ((key=='/' || key=='?') && !bullet2fired)
      {
        bullet2fired=true;
        bullet2x=tank2x;
        bullet2y=tank2y; 
        bullet2dx=bulletspeed*Math.cos(tank2angle);
        bullet2dy=-bulletspeed*Math.sin(tank2angle);
        bullet2count=bulletlife;
        shot.play();
      }
    }
    return true;
  }


  public boolean keyUp(Event e, int key)
  {
    if (key=='d' || key=='D' || key=='a' || key=='A')
      tank1dangle=0;
    if (key=='4' || key=='6' || key==Event.LEFT || key==Event.RIGHT)
      tank2dangle=0;
    if (key=='w' || key=='W' || key=='s' || key=='S')
      tank1drive=0;
    if (key=='8' || key==Event.UP || key=='2' || key==Event.DOWN)
      tank2drive=0;
    if (key==Event.ESCAPE)
    {
      ingame=false;
    }
    if (!ingame)
    {
      if (key=='1')
      {
        ingame=true;
        steerablebullets=true;
        player1score = 0;
        player2score = 0;
      }
      if (key=='2')
      {
        ingame=true;
        steerablebullets=false;
        player1score = 0;
        player2score = 0;
      }
    }
    return true;
  }


  public void paint(Graphics g)
  {
    if (goff==null && d.width>0 && d.height>0)
    {
      ii = createImage(d.width, d.height);
      goff = ii.getGraphics();
    }
    if (goff==null || ii==null)
      return;

    goff.setColor(backgnd);
    goff.fillRect(0, 0, d.width, d.height);

    if (ingame)
      PlayGame();
    else
      ShowIntroScreen();
    g.drawImage(ii, 0, 0, this);
  }


  public void DoBullets()
  {
    if (bullet1fired)
    {
      goff.setColor(tank1color);
      goff.fillRect((int)bullet1x,(int)bullet1y,2,2);

      if (bullet1x>(tank2x-collisionrange) && bullet1x<(tank2x+collisionrange) &&
          bullet1y>(tank2y-collisionrange) && bullet1y<(tank2y+collisionrange))
      {
        tank2dying=true;
        player1score++;
        explosion.play();
      }
      if (steerablebullets)
      {
        bullet1dx=bulletspeed*Math.cos(tank1angle);
        bullet1dy=-bulletspeed*Math.sin(tank1angle);
      }
      bullet1x+=bullet1dx;
      bullet1y+=bullet1dy;
      bullet1count--;
      if (bullet1count<=0 || CheckBullet(bullet1x, bullet1y))
      {
        bullet1fired=false;
      }
    }
    if (bullet2fired)
    {
      goff.setColor(tank2color);
      goff.fillRect((int)bullet2x,(int)bullet2y,2,2);
      if (bullet2x>(tank1x-collisionrange) && bullet2x<(tank1x+collisionrange) &&
          bullet2y>(tank1y-collisionrange) && bullet2y<(tank1y+collisionrange))
      {
        tank1dying=true;
        player2score++;
        explosion.play();
      }
      if (steerablebullets)
      {
        bullet2dx=bulletspeed*Math.cos(tank2angle);
        bullet2dy=-bulletspeed*Math.sin(tank2angle);
      }
      bullet2x+=bullet2dx;
      bullet2y+=bullet2dy;
      bullet2count--;
      if (bullet2count<=0 || CheckBullet(bullet2x, bullet2y))
      {
        bullet2fired=false;
      }
    }
  }


  public boolean CheckBullet(double x, double y)
  {
    boolean rc=false;
    int     j=0;

    while(blockers[j]>-0.5 && !rc)
    {
      if (x>=blockers[j] && x<blockers[j+2] &&
          y>=blockers[j+1] && y<blockers[j+3])
      {
        rc=true;
      }
      j+=4;
    }
    return rc;
  }


  public void CalcTank(double x, double y, double angle)
  {
    int		i;

    for (i=0; i<polycoords; i++)
    {
      xcoord[i] = (int)(x+(tankpoly_x[i]*Math.sin(angle)-tankpoly_y[i]*Math.cos(angle)));
      ycoord[i] = (int)(y+(tankpoly_x[i]*Math.cos(angle)+tankpoly_y[i]*Math.sin(angle))); 
    }
  }


  public void MoveTank(double[] coord)
  {
    double	dx,dy;
    double	x=coord[0],y=coord[1];
    double	angle=coord[2]+coord[3];
    int		i,j;
    boolean	hitwall=false;

    if (angle>(2*pi))
      angle-=(2*pi);
    if (angle<0)
      angle+=(2*pi);

    dx=tankspeed*Math.cos(angle);
    dy=-tankspeed*Math.sin(angle);
 
    if (coord[4]>0.5)
    {
      x+=dx;
      y+=dy;
    } else if (coord[4]<-0.5)
    {
      x-=dx;
      y-=dy;
    }

    CalcTank(x,y,angle);	// determine where the tank coordinates will be

    for (i=0; i<polycoords; i++) // now we're going to check whether the tank drives into a wall
    {
      j=0;
      while(blockers[j]>-0.5)
      {
        if (xcoord[i]>blockers[j] && xcoord[i]<blockers[j+2] &&
	    ycoord[i]>blockers[j+1] && ycoord[i]<blockers[j+3])
        {
          hitwall=true;
        }
        j+=4;
      }
    }
    if (!hitwall)
    {
      coord[0]=x;
      coord[1]=y;
      coord[2]=angle;
    }
  }


  public void DoTanks()
  {
    double	distance;
    double	temp[] = {0.0, 0.0, 0.0, 0.0, 0.0};  // why that stupid java doesn't have a call by reference
					   	// is beyond me.... I hate doing work arounds

    distance=Math.sqrt((tank1x-tank2x)*(tank1x-tank2x)+(tank1y-tank2y)*(tank1y-tank2y));

    if (distance<(collisionrange*2) && !tank1dying && !tank2dying) // did the two tanks collide
    {
      player1score++;
      player2score++;
      tank1dying=true;
      tank2dying=true;
      explosion.play();
    }

    goff.setColor(tank1color);
    CalcTank(tank1x, tank1y, tank1angle);  // Calc coords for tank #1
    goff.fillPolygon(xcoord,ycoord,polycoords); // and draw it

    goff.setColor(tank2color);
    CalcTank(tank2x, tank2y, tank2angle);  // Calc coords for tank #2
    goff.fillPolygon(xcoord,ycoord,polycoords); // and draw it

    if (tank1dying)
    {
      tank1angle+=pi/15;
    }
    else
    {
      temp[0] = tank1x;
      temp[1] = tank1y;
      temp[2] = tank1angle; // workaround for java not being able to do call by reference
      temp[3] = tank1dangle;
      temp[4] = tank1drive;
      MoveTank(temp);       // Move the thing
      tank1x=temp[0];	    // copy x and y coords back
      tank1y=temp[1];
      tank1angle=temp[2];
    }
    if (tank2dying)
    {
      tank2angle-=pi/15;
    }
    else
    {
      temp[0] = tank2x;
      temp[1] = tank2y;
      temp[2] = tank2angle;
      temp[3] = tank2dangle;
      temp[4] = tank2drive;
      MoveTank(temp);
      tank2x=temp[0];
      tank2y=temp[1];
      tank2angle=temp[2];
    }

    
  }


  public void PlayGame()
  {
    DrawPlayField();
    ShowScore();
    DoTanks();
    if (tank1dying || tank2dying)
    {
      dyingcount--;
      if (dyingcount==0)
      GameInit();
      if (player1score==5 || player2score==5)
        ingame=false;
    }
    else
    {
      DoBullets();
    }
  }


  public void ShowIntroScreen()
  {
    String s;

    DrawPlayField();
    ShowScore();
    goff.setColor(backgnd);
    goff.fillRect(10, 40, 380, 270);
    DoTanks();

    if (showtitle)
    {
      goff.setFont(largefont);
      s="Battle Tank";
      goff.setColor(new Color(96,128,255));
      goff.drawString(s,(d.width-fmlarge.stringWidth(s)) / 2, 80); 
      goff.setFont(smallfont);
      goff.setColor(new Color(255,255,0)); 

      s="Kalahkan musuhmu :D ";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 110); 

      goff.setColor(new Color (128,255,128));
      s="Tekan 1 Peluru Belok";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 190);
      s="Tekan 2 Peluru tidak belok";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 210);
      s="First to achieve 5 hits wins";
      goff.setColor(new Color(255,96,192));
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 240);
    }
    else
    {
      goff.setFont(largefont);
      s="Battle Tank";
      goff.setColor(new Color(96,128,255));
      goff.drawString(s,(d.width-fmlarge.stringWidth(s)) / 2, 80); 

      goff.setFont(smallfont);
      goff.setColor(new Color(255,255,0));
      s="HOW TO PLAY";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 130);
      goff.setColor(new Color (128,255,128));
      s="Player 1 use [a] [d] [w] [s], and [f] for shoot";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 210);
      s="player 2 use [4] [6] [8] [2], and [/] or cursor keys";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s)) / 2, 230);
    }
    count--;
    if (count<=0)
    { 
      count=screendelay;
      showtitle=!showtitle;
    }
  }


  public void DrawPlayField()
  {
    int		i=0;

    goff.setColor(Color.white);
    while (blockers[i]>-0.5)
    {
      goff.fillRect((int)blockers[i], (int)blockers[i+1], 
		(int)(blockers[i+2]-blockers[i]), (int)(blockers[i+3]-blockers[i+1]));
      i+=4;
    }
  }


  public void ShowScore()
  {
    String s;
    goff.setFont(smallfont);
    goff.setColor(Color.white);

    s="Player 1: "+player1score;
    goff.drawString(s,15,15);
    s="Player 2: "+player2score;
    goff.drawString(s,315,15);
  }


  public void run()
  {
    long  starttime;
    Graphics g;

    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    g=getGraphics();

    while(true)
    {
      starttime=System.currentTimeMillis();
      try
      {
        paint(g);
        starttime += 20;
        Thread.sleep(Math.max(0, starttime-System.currentTimeMillis()));
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
  }

  public void start()
  {
    if (thethread == null)
    {
      thethread = new Thread(this);
      thethread.start();
    }
  }

  public void stop()
  {
    if (thethread != null)
    {
      thethread.stop();
      thethread = null;
    }
  }
}
