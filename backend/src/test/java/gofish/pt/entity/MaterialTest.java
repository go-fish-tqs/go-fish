package gofish.pt.entity;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MaterialTest {

    @Test
    public void materialsByGroup() {
        Material.MaterialGroup mg = Material.MaterialGroup.NETS;
        mg.getMaterials().forEach(m -> assertThat(m.getGroup()).isEqualTo(mg));
    }
}
