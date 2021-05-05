package ac.grim.grimac.checks.predictionengine.movementTick;

import ac.grim.grimac.checks.predictionengine.predictions.PredictionEngineNormal;
import ac.grim.grimac.checks.predictionengine.predictions.PredictionEngineWater;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.enums.MoverType;
import org.bukkit.util.Vector;

import static ac.grim.grimac.checks.predictionengine.predictions.PredictionEngine.getMovementResultFromInput;

public class MovementTickerLivingVehicle extends MovementTicker {
    Vector movementInput;

    public MovementTickerLivingVehicle(GrimPlayer grimPlayer) {
        super(grimPlayer);

        grimPlayer.clientVelocity.multiply(0.98);
    }

    @Override
    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
        Vector movementInputResult = getMovementResultFromInput(movementInput, swimSpeed, player.xRot);
        addAndMove(MoverType.SELF, movementInputResult);

        PredictionEngineWater.staticVectorEndOfTick(player, player.clientVelocity, swimFriction, player.gravity, isFalling);
    }

    @Override
    public void doLavaMove() {
        Vector movementInputResult = getMovementResultFromInput(movementInput, 0.02F, player.xRot);
        addAndMove(MoverType.SELF, movementInputResult);

        // Lava doesn't have an end of tick thing?
        //vectorEndOfTick(grimPlayer, grimPlayer.clientVelocity);
    }

    @Override
    public void doNormalMove(float blockFriction) {
        // We don't know if the horse is on the ground
        // TODO: Different friction if horse is in the air
        player.friction = blockFriction * 0.91f;

        Vector movementInputResult = getMovementResultFromInput(movementInput, player.speed, player.xRot);

        addAndMove(MoverType.SELF, movementInputResult);

        PredictionEngineNormal.staticVectorEndOfTick(player, player.clientVelocity);
    }

    public void addAndMove(MoverType moverType, Vector movementResult) {
        player.clientVelocity.add(movementResult);
        super.move(moverType, player.clientVelocity);
    }
}
