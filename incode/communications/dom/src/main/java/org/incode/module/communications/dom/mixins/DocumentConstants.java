/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.incode.module.communications.dom.mixins;

import org.incode.module.communications.dom.impl.comms.Communication;
import org.incode.module.document.dom.impl.docs.Document;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannelType;

public final class DocumentConstants {

    private DocumentConstants(){}

    public static final String MIME_TYPE_APPLICATION_PDF = "application/pdf";

    /**
     * for {@link Document}s attached to {@link CommunicationChannelType#EMAIL_ADDRESS email} {@link Communication}s
     */
    public static final String PAPERCLIP_ROLE_ATTACHMENT = "attachment";

    /**
     * for {@link Document}s attached to {@link CommunicationChannelType#EMAIL_ADDRESS email} {@link Communication}s
     */
    public static final String PAPERCLIP_ROLE_COVER = "cover";

    /**
     * for {@link Document}s attached to {@link CommunicationChannelType#POSTAL_ADDRESS postal} {@link Communication}s
     */
    public static final String PAPERCLIP_ROLE_ENCLOSED = "enclosed";


}
