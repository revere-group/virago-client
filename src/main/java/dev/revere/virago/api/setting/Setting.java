package dev.revere.virago.api.setting;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Remi
 * @project Virago
 * @date 3/22/2024
 */
@Setter
@Getter
public class Setting<T> {

    @Getter private final String name;
    @Getter private String description = "";

    private Supplier<Boolean> visibility = () -> true;

    @Getter private Setting<?> parent = null;
    @Getter private List<Setting<?>> children = new ArrayList<>();

    @Getter protected T value;
    @Getter private T defaultValue;

    @Getter private T minimum;
    @Getter private T maximum;
    @Getter private T incrementation;

    @Getter private int index = 0;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;

        if (getValue() instanceof Enum<?>) {
            index = ((Enum<?>) value).ordinal();
        }
    }

    /**
     * Get the mode of the setting.
     *
     * @param previous If the mode should be the previous mode.
     * @return The mode.
     */
    public T getMode(boolean previous) {
        if (getValue() instanceof Enum<?>) {
            Enum<?> enumeration = (Enum<?>) getValue();

            String[] values = Arrays.stream(enumeration.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);

            if (!previous) {
                index = index + 1 > values.length - 1 ? 0 : index + 1;
            } else {
                index = index - 1 < 0 ? values.length - 1 : index - 1;
            }

            return (T) Enum.valueOf(enumeration.getClass(), values[index]);
        }

        return null;
    }

    /**
     * Set the minimum value of the setting.
     *
     * @param minimum The minimum value.
     * @return The setting.
     */
    public Setting<T> minimum(T minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Set the maximum value of the setting.
     *
     * @param maximum The maximum value.
     * @return The setting.
     */
    public Setting<T> maximum(T maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * Set the incrementation value of the setting.
     *
     * @param incrementation The incrementation value.
     * @return The setting.
     */
    public Setting<T> incrementation(T incrementation) {
        this.incrementation = incrementation;
        return this;
    }

    /**
     * Set the description of the setting.
     *
     * @param description The description.
     * @return The setting.
     */
    public Setting<T> describedBy(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the visibility of the setting.
     *
     * @param visibility The visibility.
     * @return The setting.
     */
    public Setting<T> visibleWhen(Supplier<Boolean> visibility) {
        this.visibility = visibility;
        return this;
    }

    /**
     * Set the parent of the setting.
     *
     * @param parent The parent.
     * @return The setting.
     */
    public Setting<T> childOf(Setting<?> parent) {
        this.parent = parent;
        this.parent.children.add(this);

        return this;
    }

    public String getPath() {
        return getParent() == null ? getName() : getParent().getPath() + getName();
    }

    public List<Setting<?>> getHierarchy() {
        List<Setting<?>> hierarchy = new ArrayList<>();

        for (Setting<?> subsetting : getChildren()) {
            hierarchy.add(subsetting);
            hierarchy.addAll(subsetting.getHierarchy());
        }

        return hierarchy;
    }

    public boolean isVisible() {
        return visibility.get();
    }
}
