package xyz.charliezhang.starstream.entity.powerup;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import xyz.charliezhang.starstream.Assets;

import static xyz.charliezhang.starstream.Config.ATT_POWERUP_PATH;

public class AttackPowerUp extends PowerUp
{
	@Override
	public PowerUps getType() {return PowerUps.ATTACK;}
	
	public AttackPowerUp() {
		super();
		
		textureAtlas = Assets.manager.get(ATT_POWERUP_PATH, TextureAtlas.class);
		animation = new Animation(1/15f, textureAtlas.getRegions());

		sprite.setSize(22, 30);
	}
}
