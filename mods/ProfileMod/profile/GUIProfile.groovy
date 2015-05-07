package profile

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Array
import com.mygdx.game.component.Colonist
import com.mygdx.game.component.Stats
import com.mygdx.game.entity.Entity
import com.mygdx.game.helpers.Constants
import com.mygdx.game.helpers.EventSystem
import com.mygdx.game.helpers.gui.GUI
import com.mygdx.game.helpers.worldgeneration.WorldGen
import com.mygdx.game.interfaces.IScript
import com.mygdx.game.ui.PlayerInterface

/**
 * Created by Paha on 4/20/2015.
 * Another example of how to add a button to the Colonist Behaviour Component and assign it a task.
 */
class GUIProfile implements IScript{
    private Array<ColonistProfile> list = new Array<>();
    private Rectangle textureRect = new Rectangle();

    @Override
    void start() {
        EventSystem.onGameEvent("entity_created", {args ->
            Entity ent = args[0] as Entity;
            if(!ent.hasTag(Constants.ENTITY_COLONIST))
                return;

            ColonistProfile prof = new ColonistProfile();
            prof.colonist = ent.getComponent(Colonist.class);
            list.add(prof);

        })

        EventSystem.onGameEvent("render_GUI", {args ->
            float delta = args[0] as float
            SpriteBatch batch = args[1] as SpriteBatch

            float top = Gdx.graphics.getHeight()
            float left = 0
            float width = Gdx.graphics.width*0.05f
            float height = top*0.05f
            float space = 10f;

            float barX = width*0.1f
            float barY = top - top*0.03f
            float barWidth = width*0.8f
            float barHeight = height*0.3f

            list.eachWithIndex { ColonistProfile entry, int i ->
                if (!entry.colonist.isValid()) list.removeIndex(i)

                float x = left + i*width + i*space;

                Color batchColor = batch.getColor();
                batch.setColor(0.3f, 0.3f, 0.3f, 0.3f)

                textureRect.set(x, (float)(top - height), width, height)
                GUI.Texture(WorldGen.whiteTex, textureRect, batch)
                GUI.Label(entry.colonist.getName(), batch, x, (float)(top - height*0.6f), width, height)

                batch.setColor batchColor

                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && textureRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))
                    PlayerInterface.setSelected(entry.colonist.getEntityOwner())

                ArrayList<Stats.Stat> statList = entry.colonist.getStats().getStatList();

                int k = 0
                statList.each { Stats.Stat stat ->
                    if(stat.name.equals("health")) GUI.DrawBar(batch, (float)(x + barX), barY, barWidth, barHeight, stat.getCurrVal(), stat.getMaxVal(), true, null, stat.color)
                    else{
                        GUI.font.setScale 0.5f
                        GUI.DrawBar(batch, (float)(x + barX), (float)(barY - (barHeight*0.4f)*(k+1)), (float)barWidth*0.4f, (float)barHeight*0.4f, stat.getCurrVal(), stat.getMaxVal(), false, null, stat.color)
                        GUI.font.setScale 1
                        k++;
                    }
                }
            }
        })

    }

    private class ColonistProfile{
        public Colonist colonist;
    }
}
