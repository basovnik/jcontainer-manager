/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jcontainer.karaf;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BaseKarafContainerTest extends KarafContainerTest {

	protected static KarafContainer container;

	@BeforeClass
	public static void beforeClass() throws Exception {
		final KarafConfiguration conf = KarafConfiguration.builder().directory(KARAF_HOME).xmx("2g").build();
		container = new KarafContainer<>(conf);
		final KarafUser user = new KarafUser();
		user.setUsername(conf.getUsername());
		user.setPassword(conf.getPassword());
		user.addRoles("admin", "SuperUser");
		container.addUser(user);
		container.start();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (container != null) {
			container.stop();
		}
	}

	@Before
	public void before() throws Exception {
		System.out.println(container.getConfigFile(CONFIG).getAbsolutePath());
		container.getConfigFile(CONFIG).delete();
	}

	@Test
	public void successCmdTest() throws Exception {
		container.getClient().execute(GOOD_CMD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badResultCmdTest() throws Exception {
		container.getClient().execute(BAD_RESULT_CMD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badFormatCmdTest() throws Exception {
		container.getClient().execute(BAD_FORMAT_CMD);
	}

	@Test
	public void successBatchTest() throws Exception {
		final List<String> cmds = new ArrayList<>();
		cmds.add(String.format("config:edit %s", CONFIG));
		cmds.add(String.format("config:property-set %s %s", PROP_NAME, PROP_VAL));
		cmds.add("config:update");
		container.getClient().execute(cmds);
		Assert.assertTrue(container.getConfigFile(CONFIG).exists());
	}

	@Ignore("Batch rollback is not supported in Apache Karaf")
	@Test
	public void failBatchTest() throws Exception {
		final List<String> cmds = new ArrayList<>();
		try {
			cmds.add(String.format("config:edit %s", CONFIG));
			cmds.add(String.format("config:property-set %s %s", PROP_NAME, PROP_VAL));
			cmds.add("config:update");
			cmds.add(BAD_FORMAT_CMD);
			container.getClient().execute(cmds);
		} catch (Exception e) {
			Assert.assertFalse("Batch test does not work", container.getConfigFile(CONFIG).exists());
		}
	}

	@Test
	public void standaloneClientTest() throws Exception {
		try (KarafClient client = new KarafClient<>(KarafConfiguration.builder().build())) {
			client.execute(GOOD_CMD);
			Assert.assertNotNull(client.getCommandResult());
		}
	}
}

