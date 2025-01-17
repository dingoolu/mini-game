package com.custom.member;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.custom.member.constant.MemberFixtureAttribute;
import com.custom.member.constant.MemberName;
import com.custom.member.status.ChunLiStatus;
import com.custom.member.weapon.ChunLiQiGongBall;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mini.animation.MiniAnimation;
import com.mini.animation.MiniAnimationHelper;
import com.mini.animation.MiniAnimationHolder;
import com.mini.animation.MiniAnimationHolderAction;
import com.mini.assist.AnimationAssist;
import com.mini.assist.MiniAnimationHolderAssist;
import com.mini.game.MiniGame;
import com.mini.game.MiniGameConfig;
import com.mini.member.GameSpriteCategory;
import com.mini.member.MiniUserData;
import com.mini.member.helper.GameSpriteHelper;
import com.mini.member.helper.GameSpriteHolder;
import com.mini.member.helper.GameSpriteHolderHelper;
import com.mini.member.status.GameSpriteDirection;
import com.mini.tool.MiniNonBlockQueue;
import com.mini.tool.MiniReplacerTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChunLi extends Protagonist {

    private float maximalVelocityX = 2.0f, maximalVelocityY = 0.1f;

    private Map<ChunLiStatus, FixtureDef> fixtureDefMap = new HashMap<>();

    private Set<ChunLiStatus> variableFixtureDefs = Sets.newHashSet(ChunLiStatus.SQUAT, ChunLiStatus.QI_GONG);

    private Map<ChunLiStatus, Map<GameSpriteDirection, MiniAnimationHolder>> miniAnimationHolderMap = new HashMap<>();

    private MiniAnimationHolder currentMiniAnimationHolder;

    private MiniAnimationHelper miniAnimationHelper = new MiniAnimationHelper();

    private GameSpriteHolder chunLiQiGongBallHolder;

    private ChunLiStatus status, statusOri;

    private MiniNonBlockQueue<ChunLiStatus> statusPreQueue = new MiniNonBlockQueue<>();

    private MiniReplacerTool miniReplacerTool = new MiniReplacerTool<ChunLiStatus>();

    @Override
    protected Body createBody(World world, float initX, float initY) {
        GameSpriteHelper.BodyConfig config = new GameSpriteHelper.BodyConfig();
        config.setInitX(initX).setInitY(initY)
                .setBodyType(BodyDef.BodyType.DynamicBody).setWorld(world)
                .setShapeType(Shape.Type.Polygon).setDrawW(getDrawW()).setDrawH(getDrawH()).setCategory(category).setIsSensor(false)
                .setHost(this);
        return GameSpriteHelper.me.createBody(config);
    }

    @Override
    protected void preInit() {
        chunLiQiGongBallHolder = new GameSpriteHolder(world);
        chunLiQiGongBallHolder.setUpdateExecutor(gameSprite -> {
            if (!gameSprite.isAlive) {
                gameSprite.destroyFixture();
                chunLiQiGongBallHolder.pushDestroyBodyTask(() -> gameSprite);
                chunLiQiGongBallHolder.removeGameSprite(gameSprite);
            }
        });
    }

    @Override
    protected void initStatus() {
        direction = GameSpriteDirection.R;

        status = ChunLiStatus.QUIET;
        setStatusPre(status);
    }

    @Override
    protected void initFixtures() {
        initFixtureBox(ChunLiStatus.QUIET, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
        initFixtureBox(ChunLiStatus.WALK, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
        initFixtureBox(ChunLiStatus.JUMP, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
        initFixtureBox(ChunLiStatus.LANDFALL, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
        initFixtureBox(ChunLiStatus.SQUAT, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate() * 0.85f);
        initFixtureBox(ChunLiStatus.CRACKED_FEET, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate(), getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
        initFixtureBox(ChunLiStatus.QI_GONG, getDrawW() * MiniGameConfig.getPhysicalSettingMemberViewRate() * 1.4f, getDrawH() * MiniGameConfig.getPhysicalSettingMemberViewRate());
    }

    protected void initFixtureBox(ChunLiStatus st, float boxW, float boxH) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxW, boxH);
        FixtureDef fixtureDef = GameSpriteHelper.me.createFixtureDef(shape, category.bits, category.maskBits, false);
        fixtureDefMap.put(st, fixtureDef);
    }

    @Override
    protected void initAnimations() {
        initAnimationQuiet();
        initAnimationWalk();
        initAnimationJump();
        initAnimationLandfall();
        initAnimationSquat();
        initAnimationCrackedFeet();
        initAnimationQiGong();
    }

    private void initAnimationQuiet() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli7.png");
        int perW = texture.getWidth() / 4;
        int perH = texture.getHeight() / 3;
        insertAnimation(ChunLiStatus.QUIET, GameSpriteDirection.R, AnimationAssist.createAnimation(texture, Lists.newArrayList(
                new AnimationAssist.Bound(perW * 3, perH * 2, perW, perH)
        ), MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
        insertAnimation(ChunLiStatus.QUIET, GameSpriteDirection.L, AnimationAssist.createAnimation(texture, Lists.newArrayList(
                new AnimationAssist.Bound(perW * 3, perH * 2, perW, perH)
        ), MiniGameConfig.getScreenSettingFrameDuration(), 1, Animation.PlayMode.LOOP));
    }

    private void initAnimationWalk() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli7.png");
        int perW = texture.getWidth() / 4;
        int perH = texture.getHeight() / 3;
        insertAnimation(ChunLiStatus.WALK, GameSpriteDirection.R, AnimationAssist.createAnimation(texture, Lists.newArrayList(
                new AnimationAssist.Bound(perW * 0, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 0, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 0, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 3, perH * 2, perW, perH)
        ), MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
        insertAnimation(ChunLiStatus.WALK, GameSpriteDirection.L, AnimationAssist.createAnimation(texture, Lists.newArrayList(
                new AnimationAssist.Bound(perW * 0, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 0, perW, perH),
                new AnimationAssist.Bound(perW * 0, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 1, perW, perH),
                new AnimationAssist.Bound(perW * 0, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 1, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 2, perH * 2, perW, perH),
                new AnimationAssist.Bound(perW * 3, perH * 2, perW, perH)
        ), MiniGameConfig.getScreenSettingFrameDuration(), 1, Animation.PlayMode.LOOP));
    }

    private void initAnimationJump() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli1.png");
        insertAnimation(ChunLiStatus.JUMP, GameSpriteDirection.R,
                AnimationAssist.createAnimation(texture, texture.getWidth() / 4, texture.getHeight() / 2, 8, MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
        insertAnimation(ChunLiStatus.JUMP, GameSpriteDirection.L,
                AnimationAssist.createAnimation(texture, texture.getWidth() / 4, texture.getHeight() / 2, 8, MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
    }

    private void initAnimationLandfall() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli9.png");
        insertAnimation(ChunLiStatus.LANDFALL, GameSpriteDirection.R,
                AnimationAssist.createAnimation(texture, texture.getWidth() / 3, texture.getHeight() / 3, 8, MiniGameConfig.getScreenSettingFrameDuration(), 1, Animation.PlayMode.LOOP));
        insertAnimation(ChunLiStatus.LANDFALL, GameSpriteDirection.L,
                AnimationAssist.createAnimation(texture, texture.getWidth() / 3, texture.getHeight() / 3, 8, MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
    }

    private void initAnimationSquat() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli8.png");
        insertAnimation(ChunLiStatus.SQUAT, GameSpriteDirection.R,
                AnimationAssist.createAnimation(texture, Lists.newArrayList(new AnimationAssist.Bound(230, 250, 124, 104)), MiniGameConfig.getScreenSettingFrameDuration(), 1, Animation.PlayMode.LOOP));
        insertAnimation(ChunLiStatus.SQUAT, GameSpriteDirection.L,
                AnimationAssist.createAnimation(texture, Lists.newArrayList(new AnimationAssist.Bound(230, 250, 124, 104)), MiniGameConfig.getScreenSettingFrameDuration(), 0, Animation.PlayMode.LOOP));
    }

    private void initAnimationCrackedFeet() {
        Texture texture = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli4.png");
        Animation animation = AnimationAssist.createAnimation(texture, texture.getWidth() / 2, texture.getHeight() / 4, 8, MiniGameConfig.getScreenSettingFrameDuration(), 0, null);
        MiniAnimation miniAnimation = new MiniAnimation(animation, getDrawW(), getDrawH());
        MiniAnimationHolder miniAnimationHolder = MiniAnimationHolderAssist.createMiniAnimationHolder(miniAnimation);
        miniAnimationHolder.setAction(new MiniAnimationHolderAction() {
            @Override
            public void beOverrideAct(MiniAnimationHolder holder) {
                holder.resetStatus();
            }

            @Override
            public void doFinishAct(MiniAnimationHolder holder) {
                statusPreQueue.release(ChunLi.this);
            }
        });
        insertMiniAnimationHolder(ChunLiStatus.CRACKED_FEET, GameSpriteDirection.R, miniAnimationHolder);

        animation = AnimationAssist.createAnimation(texture, texture.getWidth() / 2, texture.getHeight() / 4, 8, MiniGameConfig.getScreenSettingFrameDuration(), 1, null);
        miniAnimation = new MiniAnimation(animation, getDrawW(), getDrawH());
        miniAnimationHolder = MiniAnimationHolderAssist.createMiniAnimationHolder(miniAnimation);
        miniAnimationHolder.setAction(new MiniAnimationHolderAction() {
            @Override
            public void beOverrideAct(MiniAnimationHolder holder) {
                holder.resetStatus();
            }

            @Override
            public void doFinishAct(MiniAnimationHolder holder) {
                statusPreQueue.release(ChunLi.this);
            }
        });
        insertMiniAnimationHolder(ChunLiStatus.CRACKED_FEET, GameSpriteDirection.L, miniAnimationHolder);
    }

    private void initAnimationQiGong() {
        // 右
        Texture texture1 = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli6.png");
        Animation animation1 = AnimationAssist.createAnimation(texture1, texture1.getWidth() / 3, texture1.getHeight() / 6, 13, MiniGameConfig.getScreenSettingFrameDuration(), 0, null);
        MiniAnimation miniAnimation1 = new MiniAnimation(animation1, getDrawW(), getDrawH());

        Texture texture2 = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli5.png");
        Animation animation2 = AnimationAssist.createAnimation(texture2, texture2.getWidth() / 3, texture2.getHeight() / 5, 15, MiniGameConfig.getScreenSettingFrameDuration(), 0, null);
        MiniAnimation miniAnimation2 = new MiniAnimation(animation2, getDrawW(), getDrawH());
        MiniAnimationHolder miniAnimationHolder = new MiniAnimationHolder(ChunLiStatus.QI_GONG.name() + GameSpriteDirection.R.name());
        miniAnimationHolder.putMiniAnimation(miniAnimation1);
        miniAnimationHolder.putMiniAnimation(miniAnimation2);
        miniAnimationHolder.setAction(new MiniAnimationHolderAction() {
            @Override
            public void beOverrideAct(MiniAnimationHolder holder) {
                holder.resetStatus();
            }

            @Override
            public void doFinishAct(MiniAnimationHolder holder) {
                ChunLiQiGongBall chunLiQiGongBall = new ChunLiQiGongBall();
                chunLiQiGongBallHolder.pushCreateTask(GameSpriteHolderHelper.me.buildCreateTask(
                        chunLiQiGongBall,
                        getDrawX() + getDrawW() + chunLiQiGongBall.getDrawR(),
                        getDrawY() + chunLiQiGongBall.getDrawR() * 0.5f,
                        gs -> gs.applyForceToCenter(360, 0)));

                statusPreQueue.release(ChunLi.this);
            }
        });
        insertMiniAnimationHolder(ChunLiStatus.QI_GONG, GameSpriteDirection.R, miniAnimationHolder);

        // 左
        texture1 = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli6.png");
        animation1 = AnimationAssist.createAnimation(texture1, texture1.getWidth() / 3, texture1.getHeight() / 6, 13, MiniGameConfig.getScreenSettingFrameDuration(), 1, null);
        miniAnimation1 = new MiniAnimation(animation1, getDrawW(), getDrawH());

        texture2 = MiniGame.assetManager.get(MiniGame.assertBasePath+"members/chunli5.png");
        animation2 = AnimationAssist.createAnimation(texture2, texture2.getWidth() / 3, texture2.getHeight() / 5, 15, MiniGameConfig.getScreenSettingFrameDuration(), 1, null);
        miniAnimation2 = new MiniAnimation(animation2, getDrawW(), getDrawH());
        miniAnimationHolder = new MiniAnimationHolder(ChunLiStatus.QI_GONG.name() + GameSpriteDirection.L.name());
        miniAnimationHolder.putMiniAnimation(miniAnimation1);
        miniAnimationHolder.putMiniAnimation(miniAnimation2);
        miniAnimationHolder.setAction(new MiniAnimationHolderAction() {
            @Override
            public void beOverrideAct(MiniAnimationHolder holder) {
                holder.resetStatus();
            }

            @Override
            public void doFinishAct(MiniAnimationHolder holder) {
                ChunLiQiGongBall chunLiQiGongBall = new ChunLiQiGongBall();
                chunLiQiGongBallHolder.pushCreateTask(GameSpriteHolderHelper.me.buildCreateTask(
                        chunLiQiGongBall,
                        getDrawX() - chunLiQiGongBall.getDrawR(),
                        getDrawY() + chunLiQiGongBall.getDrawR() * 0.5f,
                        gs -> gs.applyForceToCenter(-360, 0)));

                statusPreQueue.release(ChunLi.this);
            }
        });
        insertMiniAnimationHolder(ChunLiStatus.QI_GONG, GameSpriteDirection.L, miniAnimationHolder);
    }

    @Deprecated
    private void insertAnimation(ChunLiStatus chunLiStatus, GameSpriteDirection gameSpriteDirection, Animation animation) {
        insertMiniAnimationHolder(chunLiStatus, gameSpriteDirection, MiniAnimationHolderAssist.createMiniAnimationHolder(new MiniAnimation(animation, getDrawW(), getDrawH())));
    }

    private void insertMiniAnimationHolder(ChunLiStatus chunLiStatus, GameSpriteDirection gameSpriteDirection, MiniAnimationHolder miniAnimationHolder) {
        Map<GameSpriteDirection, MiniAnimationHolder> directionMap = miniAnimationHolderMap.get(chunLiStatus);
        if (directionMap == null) {
            directionMap = new HashMap<>();
        }
        directionMap.put(gameSpriteDirection, miniAnimationHolder);

        miniAnimationHolderMap.put(chunLiStatus, directionMap);
    }

    @Override
    protected void initCustom() {

    }

    @Override
    protected void update() {
        if (!needUpdate()) {
            return;
        }

        updateStatus();
        updateFixtures();
        updateAnimations();
        updateCustom();
    }

    @Override
    protected void updateStatus() {
        // 矫正速度
        float velocityX = getVelocityX();
        float velocityY = getVelocityY();
        if (velocityX > maximalVelocityX) {
            body.setLinearVelocity(maximalVelocityX, velocityY);
            velocityX = getVelocityX();
        }
        if (velocityX < -maximalVelocityX) {
            body.setLinearVelocity(-maximalVelocityX, velocityY);
            velocityX = getVelocityX();
        }

        // 纠正状态
        ChunLiStatus statusPre = status;
        if (velocityX == 0 && velocityY == 0) {
            statusPre = ChunLiStatus.QUIET;
        } else if (velocityY == 0) {
            statusPre = ChunLiStatus.WALK;
        } else {
            if (velocityY > 0) {
                statusPre = ChunLiStatus.JUMP;
            }
            if (velocityY < 0) {
                statusPre = ChunLiStatus.LANDFALL;
            }
        }
        setStatusPre(statusPre);
        statusOri = status;
        while ((statusPre = statusPreQueue.poll()) != null) {
            if (status == statusPre) {
                continue;
            }
            status = statusPre;
            break;
        }
    }

    @Override
    public void updateFixtures() {
        if (status != statusOri) {
            if (variableFixtureDefs.contains(status) || variableFixtureDefs.contains(statusOri)) {
                Fixture fixture = body.getFixtureList().first();
                body.destroyFixture(fixture);
                fixture = body.createFixture(fixtureDefMap.get(status));

                MiniUserData data = new MiniUserData();
                data.name = category.name;
                data.body = body;
                data.fixture = fixture;
                data.mine = this;

                fixture.setUserData(data);
            }
        }
    }

    @Override
    protected void updateAnimations() {
        currentMiniAnimationHolder = miniAnimationHolderMap.get(status).get(direction);
    }

    @Override
    protected void renderCustom(SpriteBatch batch, float delta) {
        miniAnimationHelper.draw(batch, delta, currentMiniAnimationHolder, getDrawX(), getDrawY());

        chunLiQiGongBallHolder.render(batch, delta);
    }

    @Override
    public void createBomb() {
        setStatusPre(ChunLiStatus.QI_GONG, this);
        action = 1L << 0;
    }

    @Override
    public void createMarioBullet() {
        setStatusPre(ChunLiStatus.CRACKED_FEET, this);
        action = 1L << 0;
    }

    @Override
    public float getPosX() {
        return body.getPosition().x;
    }

    @Override
    public float getPosY() {
        return body.getPosition().y;
    }

    @Override
    public float getDrawX() {
        return getPosX() * MiniGameConfig.getScreenSettingViewRate() - getDrawW() / 2;
    }

    @Override
    public float getDrawY() {
        return getPosY() * MiniGameConfig.getScreenSettingViewRate() - getDrawH() / 2;
    }

    @Override
    public float getDrawW() {
        return 47;
    }

    @Override
    public float getDrawH() {
        return getDrawW() * 1.7f;
    }

    @Override
    public void applyForceToCenter(float forceX, float forceY) {
        if (status == null || status.canApplyForceToCenter()) {
            body.applyForceToCenter(forceX, forceY, true);
        }
    }

    @Override
    public GameSpriteCategory getGameSpriteCategory() {
        if (category != null) {
            return category;
        }
        return GameSpriteCategory.build(MemberFixtureAttribute.CHUNLI, MemberFixtureAttribute._CHUNLI, MemberName.CHUNLI);
    }

    @Override
    public void setDirection(GameSpriteDirection direction) {
        if (status == null || status.canRedirect()) {
            this.direction = direction;
        }
    }

    public void setStatusPre(ChunLiStatus statusPre) {
        statusPre = (ChunLiStatus) miniReplacerTool.tryReplace(status, statusPre);
        statusPreQueue.offer(statusPre);
    }

    public void setStatusPre(ChunLiStatus statusPre, Object lockHolder) {
        ChunLiStatus statusAft = (ChunLiStatus) miniReplacerTool.tryReplace(status, statusPre);
        if (statusAft.equals(statusPre)) {
            statusPreQueue.offerAndLock(statusPre, lockHolder);
        }
    }
}