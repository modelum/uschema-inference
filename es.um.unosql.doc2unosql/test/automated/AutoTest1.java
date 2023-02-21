/**
 *
 */
package automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import automated.builder.UBuilder;
import automated.jsongen.JSONGen;
import es.um.unosql.uNoSQLSchema.uNoSQLSchema;

/**
 * @author dsevilla
 *
 */
public class AutoTest1
{
	@Test
	public void test1()
	{
		uNoSQLSchema schema =
			UBuilder.schema("test1")
			.entity("Book")
				.variation(1)
					.attribute("name", "string")
				.variation(2)
					.attribute("name", "string")
					.attribute("pages", "int")
			.entity("Author")
				.variation(1)
					.attribute("name", "string")
				.variation(2)
					.attribute("name", "string")
					.attribute("age", "int")
			.build();

		System.out.println(new JSONGen(schema).doGen().getJSON());

		assertEquals(true, true);
	}
}
