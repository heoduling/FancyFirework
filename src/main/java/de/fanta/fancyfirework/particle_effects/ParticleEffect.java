package de.fanta.fancyfirework.particle_effects;

import de.fanta.fancyfirework.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class ParticleEffect {

    private static final int MAX_POINTS_PER_SHAPE = 128;
    private final Location origin;
    private final ISpawnParticle spawnParticleFunc;
    private final List<Vector> shapeVectors;

    public ParticleEffect(Location origin, Vector rotation, Shape shape, ISpawnParticle spawnParticleFunc) {
        this.spawnParticleFunc = spawnParticleFunc;
        this.origin = origin;
        shape.createVectors();
        shapeVectors = shape.getVectors();
        if (rotation != null) {
            for (Vector vector : shapeVectors) {
                VectorUtils.rotateVector(vector, rotation.getX(), rotation.getY(), rotation.getZ());
            }
        }
    }

    public void draw() {
        int points = Math.min(shapeVectors.size(), MAX_POINTS_PER_SHAPE);
        for (int i = 0; i < points; i++) {
            Vector vector = shapeVectors.get((int) ((long) i * shapeVectors.size() / points));
            origin.add(vector);
            spawnParticleFunc.spawn(origin);
            origin.subtract(vector);
        }
    }

}
