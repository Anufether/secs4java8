package com.shimizukenta.secs.sml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SmlMessageTest {

	@Test
	@DisplayName("SmlMessage of")
	void testBuild() {
		
		final String sml = "S1F1 W.";
		
		try {
			SmlMessage sm = SmlMessage.of(sml);
			
			assertEquals(sm.getStream(), 1);
			assertEquals(sm.getFunction(), 1);
			assertTrue(sm.wbit());
			assertTrue(sm.secs2().isEmpty());
		}
		catch (SmlParseException e) {
			fail(e);
		}
	}

}