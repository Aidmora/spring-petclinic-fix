
package ec.edu.epn.petclinic.owner;

import ec.edu.epn.petclinic.model.NamedEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "types")
public class PetType extends NamedEntity {

}
