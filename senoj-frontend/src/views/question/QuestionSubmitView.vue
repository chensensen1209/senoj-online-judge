<template>
  <div id="questionSubmitView">
    <a-form :model="searchParams" layout="inline" style="margin-left: 0px">
      <a-form-item field="questionId" label="题号" tooltip="请输入题目Id">
        <a-input
          v-model="searchParams.questionId"
          placeholder="请输入搜索题号"
        />
      </a-form-item>
      <a-form-item field="language" label="编程语言：" style="min-width: 240px">
        <a-select v-model="searchParams.language" placeholder="选择编程语言">
          <a-option>java</a-option>
          <a-option>c++</a-option>
          <a-option disabled="disabled">后续语言待开发</a-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="outline" shape="round" status="normal" @click="doSubmit"
          >搜索
        </a-button>
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          shape="round"
          status="success"
          @click="loadData"
          >刷新
        </a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      column-resizable
      wrapper
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
        showJumper: true,
        showPageSize: true,
      }"
      @page-change="onPageChange"
      @pageSizeChange="onPageSizeChange"
    >
      <template #judgeInfo="{ record }">
        <a-space wrap>
          <a-tag
            size="medium"
            v-for="(info, index) of record.judgeInfo"
            :key="index"
            color="green"
          >
            {{
              `${
                index === "message"
                  ? "结果" + info
                  : index === "time"
                  ? "耗时：" + info + "ms"
                  : "消耗内存：" + info + "MB"
              }`
            }}
          </a-tag>
        </a-space>
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
      <template #id="{ record }">
        <a-link status="success" @click="toQuestionSubmitPage(record)"
          >{{ record.id }}
        </a-link>
      </template>
      <template #status="{ record }">
        <!--        判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）-->
        <a-tag v-if="record.status === 0" color="cyan">待判题</a-tag>
        <a-tag v-if="record.status === 1" color="green">判题中</a-tag>
        <a-tag v-if="record.status === 2" color="blue">成功</a-tag>
        <a-tag v-if="record.status === 3" color="red">失败</a-tag>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionSubmitQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

const tableRef = ref();
const dataList = ref([]);
const total = ref(0);
// 搜索请求
const searchParams = ref<QuestionSubmitQueryRequest>({
  questionId: undefined,
  language: undefined,
  pageSize: 8,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionSubmitByPageUsingPost(
    {
      ...searchParams.value,
      sortField: "createTime",
      sortOrder: "descend",
    }
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "提交号",
    slotName: "id",
    align: "center",
  },
  {
    title: "题号",
    dataIndex: "questionId",
    align: "center",
  },
  {
    title: "提交者",
    dataIndex: "userVO.userName",
    align: "center",
  },
  {
    title: "判题信息",
    slotName: "judgeInfo",
    align: "center",
  },
  {
    title: "编程语言",
    dataIndex: "language",
    align: "center",
  },
  {
    title: "提交状态",
    slotName: "status",
    align: "center",
  },
  {
    title: "创建时间",
    slotName: "createTime",
    align: "center",
  },
];
/**
 * 当前分页
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};
/**
 * 分页大小
 * @param size
 */
const onPageSizeChange = (size: number) => {
  searchParams.value = {
    ...searchParams.value,
    pageSize: size,
  };
};
const router = useRouter();

/**
 * 跳转到做题记录页面
 * @param question
 */
const toQuestionSubmitPage = (question: QuestionSubmitQueryRequest) => {
  console.log(question.id + "1111111111111111111");
  router.push({
    path: `/view/questionSubmit/${question.id}`,
  });
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};
</script>

<style scoped>
#questionSubmitView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
