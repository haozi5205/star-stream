package xyz.charliezhang.starstream.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import xyz.charliezhang.shooter.MainGame;

public abstract class Entity 
{
	protected TextureAtlas textureAtlas;
	protected Animation animation;
	protected float animationTime;
	protected Sprite sprite;
	protected Vector2 direction;
	
	protected Entity()
	{
		sprite = new Sprite();	
		direction = new Vector2();
		
		//start animation time
		animationTime = 0;
	}

	//reset method for projectile pooling
	protected void reset() {
		setDirection(0, 0);
		setPosition(0, 0);
		animationTime = 0;
	}

	protected void render(SpriteBatch sb)
	{
		//set sprite to current animation region
		if(animation != null) {
			sprite.setRegion(animation.getKeyFrame(animationTime, true));

			//add animation time
			animationTime += Gdx.graphics.getDeltaTime();
		}

		if(sprite.getX() > -sprite.getWidth() && sprite.getX() < MainGame.WIDTH && sprite.getY() > -sprite.getHeight() && sprite.getY() < MainGame.HEIGHT) {
			sprite.draw(sb);
		}
	}

	public Rectangle getBounds()
	{
		return sprite.getBoundingRectangle();
	}

	protected void update() {
		sprite.setPosition(sprite.getX() + direction.x, sprite.getY() + direction.y);
	}

	public Vector2 getPosition() {return new Vector2(sprite.getX(), sprite.getY());}

	public void setPosition(float x, float y)
	{
		sprite.setPosition(x, y);
	}

	public void setDirection(float x, float y)
	{
		direction.set(x, y);
	}

	protected void modifyDirection(float x, float y)
	{
		direction.add(x, y);
	}

	public Sprite getSprite() {return sprite;}
}
