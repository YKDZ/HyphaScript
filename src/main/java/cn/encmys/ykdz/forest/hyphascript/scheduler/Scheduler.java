package cn.encmys.ykdz.forest.hyphascript.scheduler;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Scheduler {
    public static @NotNull Optional<ScheduledTask> runTaskLater(@NotNull Consumer<ScheduledTask> task, long delay) {
        return HyphaScript.getPlugin()
                .map(instance ->
                        instance.getServer().getGlobalRegionScheduler().runDelayed(instance, task, delay)
                );
    }

    public static @NotNull Optional<ScheduledTask> runTask(@NotNull Consumer<ScheduledTask> task) {
        return HyphaScript.getPlugin()
                .map(instance ->
                        instance.getServer().getGlobalRegionScheduler().run(instance, task)
                );
    }

    public static @NotNull Optional<ScheduledTask> runTaskAtFixedRate(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
        if (delay < 0 || period < 0) {
            return Optional.empty();
        }

        return HyphaScript.getPlugin()
                .map(instance ->
                        instance.getServer().getGlobalRegionScheduler().runAtFixedRate(instance, task, delay + 1, period + 1)
                );
    }

    public static @NotNull Optional<ScheduledTask> runAsyncTaskAtFixedRate(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
        if (delay < 0 || period < 0) {
            return Optional.empty();
        }
        // delay 和 period 不能小于等于 0，故 + 1
        // 以纳秒为单位以消除这一单位的影响，形参单位则为 ticks
        return HyphaScript.getPlugin()
                .map(instance ->
                        instance.getServer().getAsyncScheduler().runAtFixedRate(instance, task, delay / 20 * 1_000_000_000 + 1, period / 20 * 1_000_000_000 + 1, TimeUnit.NANOSECONDS)
                );
    }

    public static @NotNull Optional<ScheduledTask> runAsyncTask(@NotNull Consumer<ScheduledTask> task) {
        // delay 和 period 不能小于等于 0，故 + 1
        // 以纳秒为单位以消除这一单位的影响，形参单位则为 ticks
        return HyphaScript.getPlugin()
                .map(instance ->
                        instance.getServer().getAsyncScheduler().runNow(instance, task)
                );
    }
}
