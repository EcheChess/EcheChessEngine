/*
 *    Copyright 2014 - 2017 Yannick Watier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ca.watier.echechessengine.interfaces;

import ca.watier.echechessengine.engines.GenericGameHandler;
import ca.watier.echesscommon.enums.CasePosition;
import ca.watier.echesscommon.enums.MoveMode;

/**
 * Created by yannick on 4/23/2017.
 */
public interface MoveConstraint {
    /**
     * @param from
     * @param to
     * @param gameHandler
     * @param moveMode    - Gives the full move of the piece, ignoring the other pieces
     * @return
     */
    boolean isMoveValid(CasePosition from, CasePosition to, GenericGameHandler gameHandler, MoveMode moveMode);
}
