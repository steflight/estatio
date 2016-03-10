package org.estatio.dom.alias;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

import org.incode.module.alias.dom.api.aliasable.AliasType;
import org.incode.module.alias.dom.api.aliasable.Aliasable;
import org.incode.module.alias.dom.spi.aliastype.AliasTypeRepository;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class AliasTypeFinancialRepository implements AliasTypeRepository {

    @Override
    public List<AliasType> aliasTypesFor(final Aliasable aliasable, final String atPath) {
            List<AliasTypeFinancial> types = new ArrayList<>();
            for (AliasTypeFinancial type : AliasTypeFinancial.values()) {
                for (Class clss : type.getClassAssociated()) {
                    if (clss.isAssignableFrom(aliasable.getClass())) {
                        types.add(type);
                    }
                }
            }
            return Lists.newArrayList(
                FluentIterable.from(types)
                        .transform(x -> new AliasTypeViewModelForFinancial(x))
            );
    }
}
