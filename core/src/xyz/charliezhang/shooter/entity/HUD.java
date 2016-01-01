package xyz.charliezhang.shooter.entity;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import xyz.charliezhang.shooter.MainGame;
import xyz.charliezhang.shooter.screen.GameScreen;
import xyz.charliezhang.shooter.screen.MenuScreen;

/**
 * Created by Charlie on 2015-12-19.
 */
public class HUD
{

    private EntityManager manager;
    private Stage stage;
    private Table table;
    private Table deathTable;
    private Stack stack;
    private Stack masterStack;
    private Image healthBar;
    private Image healthFill;
    private Image missileIcon;


    private TextButton btnMenu;
    private Label.LabelStyle style;
    private Label lblGameOver;
    private Label lblScore;
    private Label lblLives;


    public HUD(final EntityManager manager)
    {
        this.manager = manager;

        stage = new Stage(new StretchViewport(MainGame.WIDTH, MainGame.HEIGHT));

        healthBar = new Image(manager.getGame().manager.get("data/textures/health.png", Texture.class));
        healthFill = new Image(manager.getGame().manager.get("data/textures/healthFill.png", Texture.class));

        missileIcon = new Image(manager.getGame().manager.get("data/textures/healthFill.png", Texture.class));
        missileIcon.setWidth(20);
        missileIcon.setVisible(false);
        //TODO change icon

        table = new Table();
        stack = new Stack();
        deathTable = new Table();

        masterStack = new Stack();
        masterStack.setFillParent(true);
        stage.addActor(masterStack);
        System.out.println(masterStack.getWidth());
        System.out.println(table.getWidth());
        System.out.println(deathTable.getWidth());

        style = new Label.LabelStyle();
        style.font = manager.getGame().manager.get("hud.ttf");
        style.fontColor = Color.BLACK;
        lblScore = new Label("" + manager.getScore(), style);
        lblLives = new Label("L: " + manager.getPlayer().getLives(), style);
        lblGameOver = new Label("Game Over", style);


        Skin skin = new Skin();

        skin.addRegions((TextureAtlas) manager.getGame().manager.get("data/ui/uiskin.atlas"));
        skin.add("default-font", manager.getGame().manager.get("menu.ttf"));

        skin.load(Gdx.files.internal("data/ui/uiskin.json"));
        btnMenu = new TextButton("Back to Menu", skin);
        btnMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                dispose();
                System.out.println("START MENUSCREEN   ");
                manager.getGame().setScreen(new MenuScreen(manager.getGame()));
                event.stop();
            }
        });

        table.top().left();
        table.add(lblScore).padLeft(5).expandX().left();
        table.add(lblLives).width(50).padRight(5).right();
        table.row();
        table.add(missileIcon).height(20).expandY().left().bottom();
        table.row();
        stack.add(healthFill);
        stack.add(healthBar);
        table.add(stack).height(30).fillX().left().bottom();

        TextureRegionDrawable backDraw = new TextureRegionDrawable(new TextureRegion(manager.getGame().manager.get("data/ui/background.png", Texture.class)));
        deathTable.add(lblGameOver).padBottom(30);
        deathTable.row();
        deathTable.add(btnMenu);
        deathTable.setBackground(backDraw);
        deathTable.setVisible(false);

        masterStack.add(table);
        masterStack.add(deathTable);

        table.debug();
        deathTable.debug();
    }

    public void update(float delta)
    {
        lblScore.setText("" + manager.getScore());
        lblLives.setText("L: " + manager.getPlayer().getLives());

        healthFill.setWidth((manager.getPlayer().getHealth() + 0.0f)/manager.getPlayer().getMaxHealth()*healthBar.getWidth());

        if(manager.getPlayer().getMissileTask().isScheduled())
        {
            missileIcon.setVisible(true);
        }
        else
        {
            missileIcon.setVisible(false);
        }

        stage.act(delta);
    }

    public void render(SpriteBatch batch, BitmapFont font)
    {
        batch.end();
        stage.draw();
        batch.begin();
    }

    public void dispose()
    {
        stage.dispose();
    }

    public void death()
    {
        deathTable.setVisible(true);
        System.out.println(deathTable.getWidth());
        Gdx.input.setInputProcessor(stage);
    }
}
