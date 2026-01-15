
package ec.edu.epn.petclinic.vet;

import ec.edu.epn.petclinic.model.NamedEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 */
@Entity
@Table(name = "specialties")
public class Specialty extends NamedEntity {

}
