package xyz.charliezhang.shooter.entity.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import xyz.charliezhang.shooter.Assets;
import xyz.charliezhang.shooter.entity.EntityManager;
import xyz.charliezhang.shooter.entity.Projectile;

public class Laser extends Projectile implements Pool.Poolable
{
	static final int BLUE = 0;
	static final int ORANGE = 1;

	public Laser() {
		super();
	}

	public void init(EntityManager manager, int color) {
		super.init(manager);

		switch(color) {
			case Laser.BLUE:
				textureAtlas = Assets.manager.get("data/textures/laserB.atlas", TextureAtlas.class);
				animation = new Animation(1 / 15f, textureAtlas.getRegions());
				sprite.setSize(6, 28);
				sprite.setOrigin(3, 14);
				break;
			case Laser.ORANGE:
				textureAtlas = Assets.manager.get("data/textures/laserO.atlas", TextureAtlas.class);
				animation = new Animation(1 / 15f, textureAtlas.getRegions());
				sprite.setSize(18, 18);
				sprite.setOrigin(9, 9);
				break;
			default:
				textureAtlas = Assets.manager.get("data/textures/laserB.atlas", TextureAtlas.class);
				animation = new Animation(1 / 15f, textureAtlas.getRegions());
				sprite.setSize(6, 28);
				sprite.setOrigin(3, 14);
		}
	}

	public void reset() {
		super.reset();
	}
}
