package me.charliezhang.starstream.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.charliezhang.starstream.Assets;
import me.charliezhang.starstream.GameData;
import me.charliezhang.starstream.MainGame;
import me.charliezhang.starstream.entity.enemies.Enemy;
import me.charliezhang.starstream.entity.enemies.EnemyLaser;
import me.charliezhang.starstream.entity.player.*;
import me.charliezhang.starstream.entity.powerup.AttackPowerUp;
import me.charliezhang.starstream.entity.powerup.MissilePowerUp;
import me.charliezhang.starstream.entity.powerup.PowerUp;
import me.charliezhang.starstream.entity.powerup.ShieldPowerUp;
import me.charliezhang.starstream.misc.Background;
import me.charliezhang.starstream.music.MusicPlayer;

import static me.charliezhang.starstream.Config.*;
import static me.charliezhang.starstream.entity.powerup.PowerUp.PowerUps.ATTACK;
import static me.charliezhang.starstream.entity.powerup.PowerUp.PowerUps.MISSILE;
import static me.charliezhang.starstream.entity.powerup.PowerUp.PowerUps.SHIELD;

public class EntityManager 
{
	private Array<Enemy> enemies;
	private Array<Projectile> playerProjectiles;
	private Array<Projectile> enemyProjectiles;
	private Array<PowerUp> powerups;
	private Array<Explosion> explosions;
	private Array<Coin> coins;

	//pools
	private Pool<EnemyLaser> enemyLaserPool;
	private Pool<Missile> missilePool;
	private Pool<Laser> laserPool;
	private Pool<Explosion> explosionPool;
	private Pool<Coin> coinPool;

	private Player player;

	private Background background;
	private me.charliezhang.starstream.misc.HUD hud;
	private Viewport viewport;

	private int currPowerupWait;
	private PowerUp.PowerUps nextPowerup;

	private boolean deathProcedure;
	private boolean spawningBoss;

	private int score;
	private int money;
	private long startTime;
	private int enemyModifier;
	private long bossMusicTimer;

	private boolean pause;
	private boolean canDispose;

	private MainGame game;

	public EntityManager(Viewport viewport, MainGame game, Background background, int level)
	{
		this.game = game;
		this.background = background;
		this.viewport = viewport;

		canDispose = false;

		enemyModifier = (level - 1) * 3;
		enemies = new Array<Enemy>();
		playerProjectiles = new Array<Projectile>();
		enemyProjectiles = new Array<Projectile>();
		powerups = new Array<PowerUp>();
		explosions = new Array<Explosion>();
		coins = new Array<Coin>();

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

		explosionPool = new Pool<Explosion>() {
			@Override
			protected Explosion newObject() {
				return new Explosion();
			}
		};

		coinPool = new Pool<Coin>() {
			@Override
			protected Coin newObject() {
				return new Coin();
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
		player.applyUpgrades();

		hud = new me.charliezhang.starstream.misc.HUD(this);

		score = 0;
		money = 0;
		startTime = System.nanoTime();
		deathProcedure = false;

		currPowerupWait = (int) (Math.random()*2 + 8);
		int nextPowerupRand = (int) (Math.random() * PowerUp.PowerUps.values().length);
		nextPowerup = PowerUp.PowerUps.values()[nextPowerupRand];

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
			MusicPlayer.stop(MusicPlayer.GAME);
			deathProcedure = true;
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
		for(Coin c : coins) {
			c.update();
			if(c.expired()) {
				coins.removeValue(c, false);
			}
		}


		//remove lasers
		for(Projectile p : playerProjectiles)
		{
			if(p.checkEnd())
			{
				playerProjectiles.removeValue(p, false);
			}
		}
		//remove enemy lasers
		for(Projectile p : enemyProjectiles)
		{
			if(p.checkEnd())
			{
				enemyProjectiles.removeValue(p, false);
			}
		}
		//remove enemies
		for(Enemy e : enemies) {
			if (e.isDead()) {
				score += e.getScore();

				Explosion exp = explosionPool.obtain();
				exp.init(2, this);
				exp.setPosition(e.getPosition().x+e.getSprite().getWidth()/2, e.getPosition().y+e.getSprite().getHeight()/2);
				spawnExplosion(exp);

				for(int i = 0; i < e.getCoin(); i ++) {
					Coin coin = coinPool.obtain();
					coin.init(this);
					coin.setPosition(e.getPosition().x+e.getSprite().getWidth()/2, e.getPosition().y+e.getSprite().getHeight()/2);
					spawnCoin(coin);
				}

				enemies.removeValue(e, false);
				Assets.manager.get(EXPLOSION_SOUND_PATH, Sound.class).play(MusicPlayer.VOLUME); //explosion

				currPowerupWait--;
				if(currPowerupWait <= 0) {
					PowerUp a;
					if(nextPowerup == MISSILE) {
						a = new MissilePowerUp(this);
						a.setDirection(-2, -2);
					} else if(nextPowerup == SHIELD) {
						a = new ShieldPowerUp(this);
						a.setDirection(-2, 2);
					} else {
						a = new AttackPowerUp(this);
						a.setDirection(2, 2);
					}
					a.setPosition(e.getPosition().x, e.getPosition().y);
					spawnPowerUp(a);
					currPowerupWait = (int) (Math.random()*5 + 8);
					int nextPowerupRand = (int) (Math.random() * PowerUp.PowerUps.values().length);
					nextPowerup = PowerUp.PowerUps.values()[nextPowerupRand];
				}
			}
			else if(e.isSuicide())
			{
				enemies.removeValue(e, false);
			}
		}
		
		//check collisions
		checkCollisions();

		//play boss music
		if(spawningBoss && System.currentTimeMillis() - bossMusicTimer > 3000) {
			spawningBoss = false;
			MusicPlayer.loop(MusicPlayer.BOSS);
		}
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
		for(Coin c : coins) {
			c.render(sb);
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
					Explosion exp = explosionPool.obtain();
					if(player instanceof BlueFury) exp.init(1, this);
					else exp.init(3, this);
					exp.setPosition(p.getPosition().x + p.getSprite().getWidth()/2, p.getPosition().y + p.getSprite().getHeight()/2);
					spawnExplosion(exp);

					//do damage
					if(p instanceof Missile) {
						e.modifyHealth(-player.getMissileDamage());
					} else{
						e.modifyHealth(-player.getDamage());
					}

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
			for(Coin c : coins) { //check player-coin collisions
				if(player.getBounds().overlaps(c.getBounds())) {
					money++;
					coins.removeValue(c, false);
				}
			}
		}
	}

	public void dispose() {
		//dispose stage in hud
		hud.dispose();
	}

	public void pause()
	{
		if(pause) {
			pause = false;
			hud.pause(false);
			MusicPlayer.resume(MusicPlayer.GAME);
		}
		else {
			hud.pause(true);
			pause = true;
			MusicPlayer.pause(MusicPlayer.GAME);
		}
	}

	public void win()
	{
		player.setDirection(0, 4);
		player.setControllable(false);
		hud.win();
		for(Coin c : coins) {
			money++;
			c.win();
		}
	}

	public void spawnEnemy(Enemy enemy) {
		if(enemy.isBoss()) {
			MusicPlayer.stop(MusicPlayer.GAME);
			Assets.manager.get(BOSS_WARNING_PATH, Sound.class).play(MusicPlayer.VOLUME);
			spawningBoss = true;
			bossMusicTimer = System.currentTimeMillis();
		}
		enemy.setEntityManager(this);
		enemy.reposition();
		enemy.applyUpgrades();
		enemies.add(enemy);
	}
	public void spawnLaser(Projectile p) {playerProjectiles.add(p);}
	public void spawnEnemyLaser(Projectile p) {enemyProjectiles.add(p);}
	private void spawnPowerUp(PowerUp p) {powerups.add(p);}
	public void spawnExplosion(Explosion e) {explosions.add(e);}
	private void spawnCoin(Coin c) {coins.add(c);}
	public void activatePowerUp(PowerUp.PowerUps p) { hud.activatePowerUp(p); }
	public void deactivatePowerUp(PowerUp.PowerUps p) { hud.deactivatePowerUp(p); }
	
	public Array<Enemy> getEnemies() {
		return enemies;
	}
	public int getTime() { return (int)((System.nanoTime() - startTime) / 1000000000); }

	public Pool<EnemyLaser> getEnemyLaserPool() {
		return enemyLaserPool;
	}
	public Pool<Laser> getLaserPool() {
		return laserPool;
	}
	public Pool<Missile> getMissilePool() {
		return missilePool;
	}
	public Pool<Explosion> getExplosionPool() { return explosionPool; }
	
	public Player getPlayer() { return player; }
	public MainGame getGame() { return game; }
	public Background getBackground() { return background; }
	public int getEnemyModifier() {return enemyModifier; }
	public int getScore() { return score; }
	public int getMoney() { return money; }
	public Viewport getViewport() { return viewport; }
	public boolean isPaused() { return pause; }
	public boolean canDispose() {return canDispose; }
	public void canDispose(boolean can) {canDispose = can; }
}

