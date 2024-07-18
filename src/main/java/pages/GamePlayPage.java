package pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.alttester.AltDriver;
import com.alttester.AltObject;
import com.alttester.Commands.FindObject.AltFindObjectsParams;
import com.alttester.Commands.FindObject.AltWaitForObjectsParams;
import com.alttester.Commands.ObjectCommand.AltCallComponentMethodParams;

public class GamePlayPage extends BasePage {


    public GamePlayPage(AltDriver driver) {
        super(driver);
    }

    public AltObject getPauseButton(){
        AltFindObjectsParams par=new AltFindObjectsParams.Builder(AltDriver.By.PATH, "//Game/WholeUI/pauseButton").build();
        AltWaitForObjectsParams params = new AltWaitForObjectsParams.Builder(par).withTimeout(10).build();
        return getDriver().waitForObject(params);
    }


    public AltObject getCharacter(){
        AltFindObjectsParams par = new AltFindObjectsParams.Builder(AltDriver.By.NAME, "PlayerPivot").build();
        AltWaitForObjectsParams params = new AltWaitForObjectsParams.Builder(par).withTimeout(10).build();
        return getDriver().waitForObject(params);
    }

    @Override
    public boolean isDisplayed(){
        if(getPauseButton() != null && getCharacter() != null){
            return true;
        }
        return false;
    }

    public void pressPause(){
        getPauseButton().tap();
    }

    public int getCurrentLife() throws Exception {
        AltObject character = getCharacter();
        if (character ==null)
        {
            throw new NullPointerException("Character not found");
        }

        return character.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController",
                                "get_currentLife", "Assembly-CSharp", new Object[] {}).build(), Integer.class);
    }


    public void avoidObstacles(int nrOfObstacles) throws Exception {
        AltObject character1 = getCharacter();
        if (character1 == null) {
            throw new NullPointerException("Character not found at the start.");
        }

        boolean movedLeft = false;
        boolean movedRight = false;

        for (int i = 0; i < nrOfObstacles; i++) {

            AltFindObjectsParams params = new AltFindObjectsParams.Builder(AltDriver.By.NAME, "Obstacle").build();
            List<AltObject> allObstacles = new ArrayList<>(Arrays.asList(getDriver().findObjectsWhichContain(params)));

            allObstacles.sort((x, y) -> {
                if (x.worldZ == y.worldZ) return 0;
                return x.worldZ > y.worldZ ? 1 : -1;
            });

            List<AltObject> toBeRemoved = new ArrayList<>();
            for (AltObject obs : allObstacles) {
                if (obs.worldZ < character1.worldZ)
                    toBeRemoved.add(obs);
            }
            allObstacles.removeAll(toBeRemoved);

            AltObject obstacle = allObstacles.get(0);

            long startTime = System.currentTimeMillis();
            while (obstacle.worldZ - character1.worldZ > 5 && (System.currentTimeMillis() - startTime) < 15000) {
                params = new AltFindObjectsParams.Builder(AltDriver.By.ID, "" + obstacle.id).build();
                obstacle = getDriver().findObject(params);

                params = new AltFindObjectsParams.Builder(AltDriver.By.NAME, "PlayerPivot").build();
                character1 = getDriver().findObject(params);

            }

            if (obstacle.name.contains("ObstacleHighBarrier")) {
                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "Slide", "Assembly-CSharp", new Object[]{}).build(), Void.class);
            } else if (obstacle.name.contains("ObstacleLowBarrier") || obstacle.name.contains("Rat")) {
                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "Jump", "Assembly-CSharp", new Object[]{}).build(), Void.class);
            } else {
                if (allObstacles.size() > 1 && obstacle.worldZ == allObstacles.get(1).worldZ) {

                    if (obstacle.worldX == character1.worldX) {

                        if (allObstacles.get(1).worldX == -1.5f) {

                            character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{1}).build(), Void.class);
                            movedRight = true;
                        } else {

                            character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{-1}).build(), Void.class);
                            movedLeft = true;
                        }
                    } else {
                        if (allObstacles.get(1).worldX == character1.worldX) {

                            if (obstacle.worldX == -1.5f) {
                                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{1}).build(), Void.class);
                                movedRight = true;
                            } else {
                                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{-1}).build(), Void.class);
                                movedLeft = true;
                            }
                        }
                    }
                } else {
                    if (obstacle.worldX == character1.worldX) {
                        character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{1}).build(), Void.class);
                        movedRight = true;
                    }
                }
            }


            startTime = System.currentTimeMillis();
            while (character1.worldZ - 3 < obstacle.worldZ && character1.worldX < 99 && (System.currentTimeMillis() - startTime) < 15000) {
                params = new AltFindObjectsParams.Builder(AltDriver.By.ID, "" + obstacle.id).build();
                obstacle = getDriver().findObject(params);
                if (obstacle == null) {
                    break;
                }
                params = new AltFindObjectsParams.Builder(AltDriver.By.NAME, "PlayerPivot").build();
                character1 = getDriver().findObject(params);
            }

            if (movedRight) {
                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{-1}).build(), Void.class);
                movedRight = false;
            }
            if (movedLeft) {
                character1.callComponentMethod(new AltCallComponentMethodParams.Builder("CharacterInputController", "ChangeLane", "Assembly-CSharp", new Object[]{1}).build(), Void.class);
                movedLeft = false;
            }
        }
    }



}
