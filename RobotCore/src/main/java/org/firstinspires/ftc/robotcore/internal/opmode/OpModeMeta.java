/*
 * Copyright (c) 2016 Robert Atkinson
 *
 *    Ported from the Swerve library by Craig MacFarlane
 *    Based upon contributions and original idea by dmssargent.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Robert Atkinson nor the names of his contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.robotcore.internal.opmode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link OpModeMeta} provides information about an OpMode.
 */
public class OpModeMeta
    {
    //----------------------------------------------------------------------------------------------
    // Types and constants
    //----------------------------------------------------------------------------------------------

    public enum Flavor { AUTONOMOUS, TELEOP }
    public enum Source { ANDROID_STUDIO, BLOCKLY, ONBOTJAVA}

    // arbitrary, but unlikely to be used by users. Sorts early
    public static final String DefaultGroup = "$$$$$$$";

    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    public final @NonNull  Flavor flavor;
    public final @NonNull  String group;
    public final @NonNull  String name;
    public final @Nullable String autoTransition;
    public final @Nullable Source source;

    private OpModeMeta(Flavor flavor, String group, String name, String autoTransition, Source source)
        {
        this.flavor = flavor;
        this.group = group;
        this.name = name;
        this.autoTransition = autoTransition;
        this.source = source;
        }

    //----------------------------------------------------------------------------------------------
    // Formatting
    //----------------------------------------------------------------------------------------------

    // Format as name for convenient use in dialogs
    @Override public String toString()
        {
        return this.name;
        }

    //----------------------------------------------------------------------------------------------
    // Comparison
    //----------------------------------------------------------------------------------------------

    // Equate only by name for convenient use in legacy code here
    @Override public boolean equals(Object o)
        {
        if (o instanceof OpModeMeta)
            {
            return this.name.equals(((OpModeMeta)o).name);
            }
        else
            return false;
        }

    @Override public int hashCode()
        {
        return this.name.hashCode();
        }

    //----------------------------------------------------------------------------------------------
    // Builder
    //----------------------------------------------------------------------------------------------
    public static class Builder
        {
        public @NonNull  Flavor flavor = Flavor.TELEOP;
        public @NonNull  String group = DefaultGroup;
        public @NonNull  String name = "";
        public @Nullable String autoTransition = null;
        public @Nullable Source source = null;

        public Builder (){};

        public Builder(OpModeMeta existing)
            {
            this.flavor = existing.flavor;
            this.group = existing.group;
            this.name = existing.name;
            this.autoTransition = existing.autoTransition;
            this.source = existing.source;
            }

        public Builder setFlavor(Flavor flavor)
            {
            this.flavor = flavor;
            return this;
            }

        public Builder setGroup(String group)
            {
            this.group = group;
            return this;
            }

        public Builder setName(String name)
            {
            this.name = name;
            return this;
            }

        public Builder setTransitionTarget(String autoTransition)
            {
            this.autoTransition = autoTransition;
            return this;
            }

        public Builder setSource(Source source)
            {
            this.source = source;
            return this;
            }

        public OpModeMeta build()
            {
            return new OpModeMeta(flavor, group, name, autoTransition, source);
            }

        public static Builder wrap(OpModeMeta existing)
            {
            return new Builder(existing);
            }
        }
    }
