package xyz.charliezhang.shooter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.Viewport;
import xyz.charliezhang.shooter.Assets;
import xyz.charliezhang.shooter.GameData;
import xyz.charliezhang.shooter.MainGame;
import xyz.charliezhang.shooter.misc.*;
import xyz.charliezhang.shooter.entity.enemies.Enemy;
import xyz.charliezhang.shooter.entity.enemies.EnemyLaser;
import xyz.charliezhang.shooter.entity.player.*;
import xyz.charliezhang.shooter.entity.powerup.AttackPowerUp;
import xyz.charliezhang.shooter.entity.powerup.MissilePowerUp;
import xyz.charliezhang.shooter.entity.powerup.PowerUp;
import xyz.charliezhang.shooter.entity.powerup.ShieldPowerUp;
import xyz.charliezhang.shooter.music.MusicPlayer;

public class EntityManager 
{
	private Array<Enemy> enemies;
	private Array<Projectile> playerProjectiles;
	private Array<Projectile> enemyProjectiles;
	private Array<PowerUp> powerups;
	private Array<Explosion> explosions;

	//pools
	private Pool<EnemyLaser> enemyLaserPool;
	private Pool<Missile> missilePool;
	private Pool<Laser> laserPool;

	private Player player;

	private Background background;
	private xyz.charliezhang.shooter.misc.HUD hud;
	private Viewport viewport;

	private int nextATT;
	private int currATT;
	private int nextMIS;
	private int currMIS;
	private int nextSHD;
	private int currSHD;

	private boolean deathProcedure;

	private int score;
	private long startTime;

	private boolean pause;

	private MainGame game;

	public static final int NUM_TYPES = 2;
	public static final int PLAYER_BLUE = 1;
	public static final int PLAYER_RED = 2;

	public static String getShipName(int type)
	{
		switch(type)
		{
			case PLAYER_BLUE: return "Blue Fury";
			case PLAYER_RED: return "Red Dragon";
			default: return "Something went wrong...";
		}
	}
	
	public EntityManager(Viewport viewport, MainGame game, Background background)
	{
		this.game = game;
		this.background = background;
		this.viewport = viewport;

		enemies = new Array<Enemy>();
		playerProjectiles = new Array<Projectile>();
		enemyProjectiles = new Array<Projectile>();
		powerups = new Array<PowerUp>();
		explosions = new Array<Explosion>();

		enemyLaserPool = new Pool<EnemyLaser>() {
			@Override
			protected EnemyLaser newObject() {
				return new EnemyLaser();
			}
		};

		laserPool = new Pool<Laser>() {
			@Override
			protected Laser newObject() {
				return new Laser();
			}
		};

		missilePool = new Pool<Missile>() {
			@Override
			protected Missile newObject() {
				return new Missile();
			}
		};

		switch (GameData.prefs.getInteger("playerType"))
		{
			case PLAYER_BLUE:
				player = new BlueFury(this);
				break;
			case PLAYER_RED:
				player = new RedDragon(this);
				break;
			default:
				player = new BlueFury(this);
		}

		hud = new xyz.charliezhang.shooter.misc.HUD(this);

		score = 0;
		startTime = System.nanoTime();
		deathProcedure = false;

		nextATT = (int) (Math.random()*2) + 3;
		nextMIS = (int) (Math.random()*2) + 4;
		nextSHD = (int) (Math.random()*2) + 2;
		currATT = 0;
		currMIS = 0;
		currSHD = 0;

		pause = false;

		InputMultiplexer mult = new InputMultiplexer();
		mult.addProcessor(hud.getStage());
		mult.addProcessor(player.getInputProcessor());
		Gdx.input.setInputProcessor(mult);
	}

	public void updateViewport(Viewport viewport)
	{
		this.viewport = viewport;
	}
	
	public void update(float delta)
	{
		hud.update(delta);

		if(pause) return;

		//update entities
		player.update();
		if(player.isDead() && !deathProcedure)
		{
			MusicPlayer.stop("game");
			deathProcedure = true;
			spawnExplosion(new Explosion(2));
			hud.death();
		}
		for(Enemy e : enemies)
		{
			e.update();
		}
		for(Projectile p : playerProjectiles)
		{
			p.update();
		}
		for(Projectile p : enemyProjectiles)
		{
			p.update();
		}
		for(PowerUp p : powerups)
		{
			p.update();
		}
		for(Explosion e : explosions)
		{
			e.update();
			if(e.isDone()) explosions.removeValue(e, false);
		}


		//remove lasers
		for(Projectile p : playerProjectiles)
		{
			if(p.checkEnd())
			{
				p.dispose();
				playerProjectiles.removeValue(p, false);
			}
		}
		//remove enemy lasers
		for(Projectile p : enemyProjectiles)
		{
			if(p.checkEnd())
			{
				p.dispose();
				enemyProjectiles.removeValue(p, false);
			}
		}
		//remove enemies
		for(Enemy e : enemies) {
			if (e.isDead()) {
				score += e.getScore();

				Explosion exp = new Explosion(2);
				exp.setPosition(e.getPosition().x+e.getSprite().getWidth()/2, e.getPosition().y+e.getSprite().getHeight()/2);
				spawnExplosion(exp);

				e.dispose();
				enemies.removeValue(e, false);
				Assets.manager.get("data/sounds/explosion.wav", Sound.class).play(MusicPlayer.VOLUME); //explosion

				currATT++;
				currSHD++;
				currMIS++;
				if(currMIS >= nextMIS) {
					MissilePowerUp a = new MissilePowerUp();
					a.setPosition(e.getPosition().x, e.getPosition().y);
					a.setDirection(-2, -2);
					spawnPowerUp(a);
					currMIS = 0;
					nextMIS = (int)(Math.random()*3 + 6);
				}
				if (currATT >= nextATT) {
					AttackPowerUp a = new AttackPowerUp();
					a.setPosition(e.getPosition().x, e.getPosition().y);
					a.setDirection(2, 2);
					spawnPowerUp(a);
					currATT = 0;
					nextATT = (int)(Math.random()*5 + 8);

				}
				if (currSHD >= nextSHD) {
					ShieldPowerUp a = new ShieldPowerUp();
					a.setPosition(e.getPosition().x, e.getPosition().y);
					a.setDirection(-2, 2);
					spawnPowerUp(a);
					currSHD = 0;
					nextSHD = (int)(Math.random()*6 + 8);
				}
			}
			else if(e.isSuicide())
			{
				e.dispose();
				enemies.removeValue(e, false);
			}
		}
		
		//check collisions
		checkCollisions();
	}
	
	public void render(SpriteBatch sb)
	{
		//render enemies
		for(Enemy e : enemies)
		{
			e.render(sb);
		}

		for(PowerUp p : powerups)
		{
			p.render(sb);
		}

		//render player
		if(!player.isDead()) player.render(sb);

		//render enemy projectiles
		for(Projectile p : enemyProjectiles)
		{
			p.render(sb);
		}
		//render player projectiles
		for(Projectile p : playerProjectiles)
		{
			p.render(sb);
		}

		for(Explosion e : explosions)
		{
			e.render(sb);
		}

		hud.render(sb);
	}
	
	private void checkCollisions()
	{
		for(Enemy e : enemies)
		{
			//check laser-enemy collision
			for(Projectile p : playerProjectiles)
			{
				if(e.getBounds().overlaps(p.getBounds()))
				{
					//explosion
					Explosion exp = new Explosion(1);
					exp.setPosition(p.getPosition().x, p.getPosition().y);
					spawnExplosion(exp);

					//do damage
					if(p instanceof Missile) e.modifyHealth(-player.getDamage()*4);
					else e.modifyHealth(-player.getDamage());

					playerProjectiles.removeValue(p, false);
				}
			}

			//check enemy-player collision
			if(player.isControllable())
			{
				if(e.getBounds().overlaps(player.getBounds()))
				{
					if(!player.isFlinching() || player.isInvincible())
					{
						if(!player.isShieldOn()) {
							player.modifyHealth(-e.getDamage());
							player.setFlinching(true);
						}
						else
						{
							player.removeShield();
						}

					}
				}
			}
		}
		if(player.isControllable())
		{
			for(Projectile p : enemyProjectiles) //check enemy laser-player collision
			{
				if(player.getBounds().overlaps(p.getBounds()))
				{
					if(!player.isFlinching())
					{
						enemyProjectiles.removeValue(p, false);
						if(!player.isShieldOn()) {
							player.modifyHealth(-((EnemyLaser)p).getEnemy().getDamage());
							player.setFlinching(true);
						}
						else
						{
							player.removeShield();
						}
					}
				}
			}
			for(PowerUp ap : powerups) //check player-powerup collision
			{
				if(player.getBounds().overlaps(ap.getBounds()))
				{
					player.activatePowerUp(ap);
					powerups.removeValue(ap, false);
				}
			}
		}
	}

	public void dispose()
	{
		player.dispose();
		for (Enemy e : enemies) {
			e.dispose();
		}
		for (Projectile p : enemyProjectiles) {
			p.dispose();
		}
		for (Projectile p : playerProjectiles) {
			p.dispose();
		}
		for (PowerUp e : powerups) {
			e.dispose();
		}
		for (Explosion e : explosions) {
			e.dispose();
		}
		hud.dispose();
	}

	public void pause()
	{
		if(pause) {
			pause = false;
			hud.pause(false);
			MusicPlayer.resume("game");
		}
		else {
			hud.pause(true);
			pause = true;
			MusicPlayer.pause("game");
		}
	}

	public void win()
	{
		player.setDirection(0, 4);
		player.setControllable(false);
	}

	public void spawnEnemy(Enemy enemy) {enemies.add(enemy);}
	public void spawnLaser(Projectile p) {playerProjectiles.add(p);}
	public void spawnEnemyLaser(Projectile p) {enemyProjectiles.add(p);}
	private void spawnPowerUp(PowerUp p) {powerups.add(p);}
	public void spawnExplosion(Explosion e) {explosions.add(e);}
	
	public Array<Enemy> getEnemies() {
		return enemies;
	}
	public int getTime() { return (int)(System.nanoTime() - startTime / 1000000000); }

	public Pool<EnemyLaser> getEnemyLaserPool() {
		return enemyLaserPool;
	}
	public Pool<Laser> getLaserPool() {
		return laserPool;
	}
	public Pool<Missile> getMissilePool() {
		return missilePool;
	}
	
	public Player getPlayer() { return player; }
	public MainGame getGame() { return game; }
	public Background getBackground() { return background; }
	public int getScore() { return score; }
	public Viewport getViewport() { return viewport; }
	public boolean isPaused() { return pause; }
}

