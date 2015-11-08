/*
 * Copyright 2013 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package online.draughts.rus.server.guice;

import com.google.inject.AbstractModule;
import online.draughts.rus.server.resource.FriendsResourceImpl;
import online.draughts.rus.server.resource.GamesResourceImpl;
import online.draughts.rus.server.resource.PlayersResourceImpl;
import online.draughts.rus.shared.resource.FriendsResource;
import online.draughts.rus.shared.resource.GamesResource;
import online.draughts.rus.shared.resource.PlayersResource;


public class RestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GamesResource.class).to(GamesResourceImpl.class);
    bind(PlayersResource.class).to(PlayersResourceImpl.class);
    bind(FriendsResource.class).to(FriendsResourceImpl.class);
  }
}
