package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.assert
import org.junit.Test

class EnumStaticInitAnalyzerTest {
    @Test
    fun basic() {
        EnumStaticInitAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final enum LEnumStaticInitBasic;
            .super Ljava/lang/Enum;
            .source "EnumStaticInitBasic.java"

            # static fields
            .field private static final synthetic ${"$"}VALUES:[LEnumStaticInitBasic;
            .field public static final enum A:LEnumStaticInitBasic;
            .field public static final enum B:LEnumStaticInitBasic;
            .field public static final enum C:LEnumStaticInitBasic;

            # direct methods
            .method static constructor <clinit>()V
                .registers 5
                .line 1
                new-instance v0, LEnumStaticInitBasic;
                const/4 v1, 0x0
                const-string v2, "REPLACE"
                invoke-direct {v0, v2, v1}, LEnumStaticInitBasic;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitBasic;->A:LEnumStaticInitBasic;
                .line 2
                new-instance v0, LEnumStaticInitBasic;
                const/4 v2, 0x1
                const-string v3, "KEEP"
                invoke-direct {v0, v3, v2}, LEnumStaticInitBasic;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitBasic;->B:LEnumStaticInitBasic;
                .line 3
                new-instance v0, LEnumStaticInitBasic;
                const/4 v3, 0x2
                const-string v4, "APPEND"
                invoke-direct {v0, v4, v3}, LEnumStaticInitBasic;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitBasic;->C:LEnumStaticInitBasic;
                const/4 v0, 0x3
                new-array v0, v0, [LEnumStaticInitBasic;
                .line 4
                sget-object v4, LEnumStaticInitBasic;->A:LEnumStaticInitBasic;
                aput-object v4, v0, v1
                sget-object v1, LEnumStaticInitBasic;->B:LEnumStaticInitBasic;
                aput-object v1, v0, v2
                sget-object v1, LEnumStaticInitBasic;->C:LEnumStaticInitBasic;
                aput-object v1, v0, v3
                sput-object v0, LEnumStaticInitBasic;->${"$"}VALUES:[LEnumStaticInitBasic;
                return-void
            .end method

            .method private constructor <init>(Ljava/lang/String;I)V
                .registers 3
                .annotation system Ldalvik/annotation/Signature;
                    value = {
                        "()V"
                    }
                .end annotation
                .line 1
                invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
                return-void
            .end method

            .method public static valueOf(Ljava/lang/String;)LEnumStaticInitBasic;
                .registers 2
                .line 1
                const-class v0, LEnumStaticInitBasic;
                invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
                move-result-object p0
                check-cast p0, LEnumStaticInitBasic;
                return-object p0
            .end method

            .method public static values()[LEnumStaticInitBasic;
                .registers 1
                .line 1
                sget-object v0, LEnumStaticInitBasic;->${"$"}VALUES:[LEnumStaticInitBasic;
                invoke-virtual {v0}, [LEnumStaticInitBasic;->clone()Ljava/lang/Object;
                move-result-object v0
                check-cast v0, [LEnumStaticInitBasic;
                return-object v0
            .end method
            """,
            mapping = """
            EnumStaticInitBasic -> EnumStaticInitBasic:
                EnumStaticInitBasic A -> REPLACE
                EnumStaticInitBasic B -> KEEP
                EnumStaticInitBasic C -> APPEND
            """.trimIndent()
        )
    }

    @Test
    fun doesntReportSameName() {
        EnumStaticInitAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final enum LEnumStaticInitSameName;
            .super Ljava/lang/Enum;
            .source "EnumStaticInitSameName.java"

            # static fields
            .field private static final synthetic ${"$"}VALUES:[LEnumStaticInitSameName;
            .field public static final enum REPLACE:LEnumStaticInitSameName;
            .field public static final enum KEEP:LEnumStaticInitSameName;
            .field public static final enum APPEND:LEnumStaticInitSameName;

            # direct methods
            .method static constructor <clinit>()V
                .registers 5
                .line 1
                new-instance v0, LEnumStaticInitSameName;
                const/4 v1, 0x0
                const-string v2, "REPLACE"
                invoke-direct {v0, v2, v1}, LEnumStaticInitSameName;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitSameName;->REPLACE:LEnumStaticInitSameName;
                .line 2
                new-instance v0, LEnumStaticInitSameName;
                const/4 v2, 0x1
                const-string v3, "KEEP"
                invoke-direct {v0, v3, v2}, LEnumStaticInitSameName;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitSameName;->KEEP:LEnumStaticInitSameName;
                .line 3
                new-instance v0, LEnumStaticInitSameName;
                const/4 v3, 0x2
                const-string v4, "APPEND"
                invoke-direct {v0, v4, v3}, LEnumStaticInitSameName;-><init>(Ljava/lang/String;I)V
                sput-object v0, LEnumStaticInitSameName;->APPEND:LEnumStaticInitSameName;
                const/4 v0, 0x3
                new-array v0, v0, [LEnumStaticInitSameName;
                .line 4
                sget-object v4, LEnumStaticInitSameName;->REPLACE:LEnumStaticInitSameName;
                aput-object v4, v0, v1
                sget-object v1, LEnumStaticInitSameName;->KEEP:LEnumStaticInitSameName;
                aput-object v1, v0, v2
                sget-object v1, LEnumStaticInitSameName;->APPEND:LEnumStaticInitSameName;
                aput-object v1, v0, v3
                sput-object v0, LEnumStaticInitSameName;->${"$"}VALUES:[LEnumStaticInitSameName;
                return-void
            .end method

            .method private constructor <init>(Ljava/lang/String;I)V
                .registers 3
                .annotation system Ldalvik/annotation/Signature;
                    value = {
                        "()V"
                    }
                .end annotation
                .line 1
                invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
                return-void
            .end method

            .method public static valueOf(Ljava/lang/String;)LEnumStaticInitSameName;
                .registers 2
                .line 1
                const-class v0, LEnumStaticInitSameName;
                invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
                move-result-object p0
                check-cast p0, LEnumStaticInitSameName;
                return-object p0
            .end method

            .method public static values()[LEnumStaticInitSameName;
                .registers 1
                .line 1
                sget-object v0, LEnumStaticInitSameName;->${"$"}VALUES:[LEnumStaticInitSameName;
                invoke-virtual {v0}, [LEnumStaticInitSameName;->clone()Ljava/lang/Object;
                move-result-object v0
                check-cast v0, [LEnumStaticInitSameName;
                return-object v0
            .end method
            """.trimIndent(),
            mapping = ""
        )
    }
}
