// Copyright © 2018 Camunda Services GmbH (info@camunda.com)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package commands

import (
	"context"
	"github.com/zeebe-io/zeebe/clients/go/internal/utils"
	"github.com/zeebe-io/zeebe/clients/go/pkg/pb"
	"time"
)

type Command struct {
	utils.SerializerMixin

	gateway        pb.GatewayClient
	requestTimeout time.Duration
	retryPredicate func(context.Context, error) bool
}

// setClientTimeout extends the context with the command's timeout, if none is set. If there was already a timeout,
//the return will be the original context and a no-op cancelFunc (that is still safe to call)
func (cmd *Command) setClientTimeout(ctx context.Context) (context.Context, context.CancelFunc) {
	return cmd.setTimeout(ctx, cmd.requestTimeout)
}

// setTimeout extends the context with the supplied timeout if none is set. If there was already a timeout, the return
// will be the original context and a no-op CancelFunc (that is still safe to call)
func (cmd *Command) setTimeout(ctx context.Context, timeout time.Duration) (context.Context, context.CancelFunc) {
	if _, hasDeadline := ctx.Deadline(); !hasDeadline {
		return context.WithTimeout(ctx, timeout)
	}

	return ctx, func() {}
}
