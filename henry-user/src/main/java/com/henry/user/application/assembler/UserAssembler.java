package com.henry.user.application.assembler;

import com.henry.common.ddd.application.assembler.BaseAssembler;
import com.henry.user.application.dto.UserDTO;
import com.henry.user.domain.model.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

/**
 * 用户组装器：领域模型 <-> 输出 DTO 映射，MapStruct 在编译期自动生成实现（Spring bean）。
 * disableBuilder=true 表示走 setter 装配，规避 @SuperBuilder 与 MapStruct 构建器探测的兼容问题。
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserAssembler extends BaseAssembler<UserDTO, User> {
}
