package ac.rino.rinoac.predictionengine.predictions.rideable;

import ac.rino.rinoac.player.RinoPlayer;
import ac.rino.rinoac.predictionengine.predictions.PredictionEngine;
import ac.rino.rinoac.predictionengine.predictions.PredictionEngineNormal;
import ac.rino.rinoac.utils.data.VectorData;
import ac.rino.rinoac.utils.data.packetentity.PacketEntityHorse;
import ac.rino.rinoac.utils.nmsutil.JumpPower;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

public class PredictionEngineRideableUtils {
    public static Set<VectorData> handleJumps(RinoPlayer player, Set<VectorData> possibleVectors) {
        if (!(player.compensatedEntities.getSelf().getRiding() instanceof PacketEntityHorse horse)) return possibleVectors;

        // Setup player inputs
        float f = player.vehicleData.vehicleHorizontal * 0.5F;
        float f1 = player.vehicleData.vehicleForward;

        if (f1 <= 0.0F) {
            f1 *= 0.25F;
        }

        // If the player wants to jump on a horse
        // Listen to Entity Action -> start jump with horse, stop jump with horse
        //
        // There's a float/double error causing 1e-8 imprecision if anyone wants to debug it
        if (player.vehicleData.horseJump > 0.0F && !player.vehicleData.horseJumping && player.lastOnGround) {
            double d0 = horse.getAttributeValue(Attributes.GENERIC_JUMP_STRENGTH) * player.vehicleData.horseJump * JumpPower.getPlayerJumpFactor(player);
            double d1;

            // This doesn't even work because vehicle jump boost has (likely) been
            // broken ever since vehicle control became client sided
            //
            // But plugins can still send this, so support it anyways
            final OptionalInt jumpBoost = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.JUMP_BOOST);
            if (jumpBoost.isPresent()) {
                d1 = d0 + ((jumpBoost.getAsInt() + 1) * 0.1F);
            } else {
                d1 = d0;
            }


            player.vehicleData.horseJumping = true;

            float f2 = player.trigHandler.sin(player.xRot * ((float) Math.PI / 180F));
            float f3 = player.trigHandler.cos(player.xRot * ((float) Math.PI / 180F));

            for (VectorData vectorData : possibleVectors) {
                vectorData.vector.setY(d1);
                if (f1 > 0.0F) {
                    vectorData.vector.add(new Vector(-0.4F * f2 * player.vehicleData.horseJump, 0.0D, 0.4F * f3 * player.vehicleData.horseJump));
                }
            }

            player.vehicleData.horseJump = 0.0F;
        }

        // More jumping stuff
        if (player.lastOnGround) {
            player.vehicleData.horseJump = 0.0F;
            player.vehicleData.horseJumping = false;
        }

        return possibleVectors;
    }

    public static List<VectorData> applyInputsToVelocityPossibilities(Vector movementVector, RinoPlayer player, Set<VectorData> possibleVectors, float speed) {
        List<VectorData> returnVectors = new ArrayList<>();

        for (VectorData possibleLastTickOutput : possibleVectors) {
            VectorData result = new VectorData(possibleLastTickOutput.vector.clone().add(new PredictionEngine().getMovementResultFromInput(player, movementVector, speed, player.xRot)), possibleLastTickOutput, VectorData.VectorType.InputResult);
            result = result.returnNewModified(result.vector.clone().multiply(player.stuckSpeedMultiplier), VectorData.VectorType.StuckMultiplier);
            result = result.returnNewModified(new PredictionEngineNormal().handleOnClimbable(result.vector.clone(), player), VectorData.VectorType.Climbable);
            returnVectors.add(result);

            // This is the laziest way to reduce false positives such as horse rearing
            // No bypasses can ever be derived from this, so why not?
            result = new VectorData(possibleLastTickOutput.vector.clone(), possibleLastTickOutput, VectorData.VectorType.InputResult);
            result = result.returnNewModified(result.vector.clone().multiply(player.stuckSpeedMultiplier), VectorData.VectorType.StuckMultiplier);
            result = result.returnNewModified(new PredictionEngineNormal().handleOnClimbable(result.vector.clone(), player), VectorData.VectorType.Climbable);
            returnVectors.add(result);
        }

        return returnVectors;
    }
}
