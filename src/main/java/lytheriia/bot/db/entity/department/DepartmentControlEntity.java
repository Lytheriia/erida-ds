package lytheriia.bot.db.entity.department;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "department_control")
public class DepartmentControlEntity {

    @EmbeddedId
    @SuppressWarnings({ "unused", "FieldMayBeFinal" })
    private DepartmentControlId id = new DepartmentControlId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("controllerId")
    @JoinColumn(name = "controller_id")
    private DepartmentEntity controller;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("controlledId")
    @JoinColumn(name = "controlled_id")
    private DepartmentEntity controlled;

    protected DepartmentControlEntity() {
    }

    public DepartmentControlEntity(DepartmentEntity controller, DepartmentEntity controlled) {
        if (controller.getId().equals(controlled.getId())) {
            throw new IllegalArgumentException("Департамент не может управлять сам собой");
        }
        if (controller.getGuildId() != controlled.getGuildId()) {
            throw new IllegalArgumentException("Департаменты из разных гильдий");
        }
        this.controller = controller;
        this.controlled = controlled;
    }

    public DepartmentEntity getController() {
        return controller;
    }

    public DepartmentEntity getControlled() {
        return controlled;
    }
}