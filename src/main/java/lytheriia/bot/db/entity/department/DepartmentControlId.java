package lytheriia.bot.db.entity.department;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DepartmentControlId implements Serializable {

    @Column(name = "controller_id")
    private Long controllerId;

    @Column(name = "controlled_id")
    private Long controlledId;

    protected DepartmentControlId() {
    }

    public DepartmentControlId(Long controllerId, Long controlledId) {
        this.controllerId = controllerId;
        this.controlledId = controlledId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DepartmentControlId that))
            return false;
        return Objects.equals(controllerId, that.controllerId)
                && Objects.equals(controlledId, that.controlledId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerId, controlledId);
    }
}