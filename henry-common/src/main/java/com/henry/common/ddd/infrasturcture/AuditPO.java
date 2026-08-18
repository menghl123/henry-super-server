package com.henry.common.ddd.infrasturcture;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.henry.common.ddd.domain.Auditable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class AuditPO implements Auditable {

    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    @TableField(value = "creator_name", fill = FieldFill.INSERT)
    private String creatorName;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    @TableField(value = "modifier_id", fill = FieldFill.INSERT_UPDATE)
    private Long modifierId;

    @TableField(value = "modifier_name", fill = FieldFill.INSERT)
    private String modifierName;

    @TableField(value = "modified_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime modifiedTime;

    @TableLogic(value = "0", delval = "1")
    @TableField(value = "deleted")
    private Boolean deleted;
}
