/*
 * Copyright (c) 2020 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.nsapi.builders;

public enum NSNationShard {

    NATION {
        @Override
        public String toString() {
            return "nation=";
        }
    },

    PROPER_NAME {
        @Override
        public String toString() {
            return "name";
        }
    },

    REGION {
        @Override
        public String toString() {
            return "region";
        }
    },

    CATEGORY {
        @Override
        public String toString() {
            return "category";
        }
    },

    ENDORSEMENT_LIST {
        @Override
        public String toString() {
            return "endorsements";
        }
    },

    CAN_RECRUIT {
        @Override
        public String toString() {
            return "tgcanrecruit";
        }
    },

    CAN_CAMPAIGN {
        @Override
        public String toString() {
            return "tgcancampaign";
        }
    },

    CENSUS {
        @Override
        public String toString() {
            return "census";
        }
    }

}
