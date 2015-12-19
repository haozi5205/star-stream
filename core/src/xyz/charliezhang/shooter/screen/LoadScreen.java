package xyz.charliezhang.shooter.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import xyz.charliezhang.shooter.MainGame;

/**
 * Created by Charlie on 2015-11-29.
 */
public class LoadScreen implements Screen
{
    private MainGame game;

    private Stage stage;

    private Image logo;
    private Image loadingFrame;
    private Image loadingBarHidden;
    private Image screenBg;
    private Image loadingBg;

    private float startX, endX, percent;

    private Actor loadingBar;

    public LoadScreen(MainGame game)
    {
        this.game = game;
    }

    @Override
    public void show() {

        //load loading ui
        game.manager.load("data/ui/loading.pack", TextureAtlas.class);
        game.manager.finishLoading();

        stage = new Stage();
        stage.setViewport(new ScreenViewport());
        TextureAtlas loadingAtlas = game.manager.get("data/ui/loading.pack", TextureAtlas.class);

        logo = new Image(loadingAtlas.findRegion("libgdx-logo"));
        loadingFrame = new Image(loadingAtlas.findRegion("loading-frame"));
        loadingBarHidden = new Image(loadingAtlas.findRegion("loading-bar-hidden"));
        screenBg = new Image(loadingAtlas.findRegion("screen-bg"));
        loadingBg = new Image(loadingAtlas.findRegion("loading-frame-bg"));
        loadingBar = new Image(loadingAtlas.findRegion("loading-bar1"));

        stage.addActor(screenBg);
        stage.addActor(loadingBar);
        stage.addActor(loadingBg);
        stage.addActor(loadingBarHidden);
        stage.addActor(loadingFrame);
        stage.addActor(logo);

        //load game assets
        game.manager.load("data/textures/background.png", Texture.class);
        game.manager.load("data/textures/health.png", Texture.class);
        game.manager.load("data/textures/healthFill.png", Texture.class);
        game.manager.load("data/textures/attpowerup.atlas", TextureAtlas.class);
        game.manager.load("data/textures/mispowerup.atlas", TextureAtlas.class);
        game.manager.load("data/textures/battleship.atlas", TextureAtlas.class);
        game.manager.load("data/textures/icarus.atlas", TextureAtlas.class);
        game.manager.load("data/textures/explosion.atlas", TextureAtlas.class);
        game.manager.load("data/textures/laser.atlas", TextureAtlas.class);
        game.manager.load("data/textures/missile.atlas", TextureAtlas.class);
        game.manager.load("data/textures/laserG.atlas", TextureAtlas.class);
        game.manager.load("data/textures/laserO.atlas", TextureAtlas.class);
        game.manager.load("data/textures/laserR.atlas", TextureAtlas.class);
        game.manager.load("data/textures/playerspritesheet.atlas", TextureAtlas.class);
        game.manager.load("data/textures/skullinator.atlas", TextureAtlas.class);
        game.manager.load("data/textures/striker.atlas", TextureAtlas.class);
        game.manager.load("data/music/background.wav", Music.class);
        game.manager.load("data/sounds/playershoot.wav", Sound.class);
        game.manager.load("data/sounds/explosion.wav", Sound.class);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(game.manager.update())
        {
            if(Gdx.input.isTouched())
            game.setScreen(new GameScreen(game));
        }

        percent = Interpolation.linear.apply(percent, game.manager.getProgress(), 0.1f);

        loadingBarHidden.setX(startX + endX * percent);
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setWidth(stage.getWidth() - stage.getWidth() * percent);
        loadingBg.invalidate();

        stage.act();
        stage.draw();

        game.batch.begin();
        game.font.draw(game.batch, game.manager.getProgress()*100 + "%", 100, 100);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        screenBg.setSize(stage.getWidth(), stage.getHeight());
        logo.setX(stage.getWidth()/2 - logo.getWidth()/2);
        logo.setY(stage.getHeight()/2 - logo.getHeight()/2 + 300);

        loadingFrame.setX(0);
        loadingFrame.setY((stage.getHeight()/2 - loadingFrame.getHeight()) / 2);
        loadingFrame.setWidth(stage.getWidth());

        loadingBar.setX(loadingFrame.getX() + 15);
        loadingBar.setY(loadingFrame.getY() + 5);
        loadingBar.setWidth(stage.getWidth());

        loadingBarHidden.setX(loadingBar.getX() + 35);
        loadingBarHidden.setY(loadingBar.getY() - 3);

        startX = loadingBarHidden.getX();
        endX = stage.getWidth();

        loadingBg.setSize(stage.getWidth() , loadingBar.getHeight());
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setY(loadingBarHidden.getY() + 3);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        game.manager.unload("data/ui/loading.pack");
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
