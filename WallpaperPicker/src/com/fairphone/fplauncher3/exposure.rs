/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#pragma version(1)
#pragma rs java_package_name(com.fairphone.fplauncher3)

static float maxBright = 255.f;
static float bright = 0.8f;

void exposure(const uchar4 *in, uchar4 *out)
{
    float pxLuma =  (in->r * 0.299)+ (in->g * 0.587)+ (in->b*0.114);

    float lumaFactor = sqrt(pxLuma/255.f*bright);

    out->r = rsClamp((int)(lumaFactor * in->r), 0, maxBright);
    out->g = rsClamp((int)(lumaFactor * in->g), 0, maxBright);
    out->b = rsClamp((int)(lumaFactor * in->b), 0, maxBright);
}
