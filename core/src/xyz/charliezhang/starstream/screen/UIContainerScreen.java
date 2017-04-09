package xyz.charliezhang.starstream.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import xyz.charliezhang.starstream.Assets;
import xyz.charliezhang.starstream.MainGame;
import xyz.charliezhang.starstream.misc.Background;
import xyz.charliezhang.starstream.music.MusicPlayer;

import static xyz.charliezhang.starstream.Config.BUTTON_SOUND_PATH;

public class UIContainerScreen implements Screen {
    private MainGame game;

    private Stage stage;
    private boolean canDispose;
    private Background background;

    private MenuTable menu;
    private OptionsTable options;
    private ShopTable shop;
    private LevelSelectTable levelSelect;

    enum UITable { MENU, OPTIONS, SHOP, LEVELSELECT };

    public UIContainerScreen(MainGame game){this.game = game;}

    @Override
    public void show() {

        stage = new Stage();
        stage.setViewport(new ExtendViewport(MainGame.WIDTH, MainGame.HEIGHT));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        canDispose = false;

        stage.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(1)));

        menu = new MenuTable(this);
        options = new OptionsTable(this);
        shop = new ShopTable(this);
        levelSelect = new LevelSelectTable(this);
        menu.setFillParent(true);
        options.setFillParent(true);
        shop.setFillParent(true);
        levelSelect.setFillParent(true);

        stage.addActor(menu);


        background = new Background();
        background.setSize(stage.getViewport().getWorldWidth());
        background.setVector(0, -2f);

        if(!MusicPlayer.isPlaying(MusicPlayer.MENU)) {
            MusicPlayer.loop(MusicPlayer.MENU);
        }

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Assets.manager.get(BUTTON_SOUND_PATH, Sound.class).play(MusicPlayer.VOLUME);
                event.stop();
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    void play(final int level) {
        stage.addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(new Runnable() {
            @Override
            public void run() {
                game.setScreen(new GameScreen(game, level + 1, background));
                canDispose = true;
            }
        })));
    }

    void changeTable(UITable table) {
        stage.getActors().first().remove();
        switch(table) {
            case MENU:
                stage.addActor(menu);
                break;
            case SHOP:
                stage.addActor(shop);
                break;
            case OPTIONS:
                stage.addActor(options);
                break;
            case LEVELSELECT:
                stage.addActor(levelSelect);
                break;
            default:
                stage.addActor(menu);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);

        //background
        game.batch.begin();
        background.update();
        background.render(game.batch);
        game.batch.end();

        stage.draw();

        if(canDispose) dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
