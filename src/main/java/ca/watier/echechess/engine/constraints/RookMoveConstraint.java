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

package ca.watier.echechess.engine.constraints;

import ca.watier.echechess.common.enums.DirectionPattern;

import java.io.Serial;

/**
 * Created by yannick on 4/23/2017.
 */
public class RookMoveConstraint extends DirectionalMoveConstraint {

    @Serial
    private static final long serialVersionUID = 8439593234072170944L;

    public RookMoveConstraint() {
        super(DirectionPattern.NORMAL);
    }
}
