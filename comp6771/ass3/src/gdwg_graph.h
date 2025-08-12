#ifndef GDWG_GRAPH_H
#define GDWG_GRAPH_H

#include <initializer_list>
#include <algorithm>
#include <iterator>
#include <limits>
#include <memory>
#include <optional>
#include <queue>
#include <set>
#include <sstream>
#include <string>
#include <utility>
#include <vector>

/**
 * Edge weight type: E
 * Node value type: N
 * Graph iterator type: InputIt, readable as type N
 */
namespace gdwg {

	template<typename N, typename E>
	class Graph; // forward declaration

	template<typename N, typename E>
	class Edge {
	 public:
		Edge(N const& src, N const& dst) noexcept
		: src_(src)
		, dst_(dst) {}

		virtual auto print_edge() const -> std::string = 0;
		virtual auto is_weighted() const -> bool = 0;
		virtual auto get_weight() const -> std::optional<E> = 0;

		auto get_nodes() const noexcept -> std::pair<N, N> {
			return std::pair<N, N>{src_, dst_};
		}

		auto operator==(Edge<N, E> const& other) const noexcept -> bool {
			if (!this->is_weighted() && other.is_weighted())
				return false;
			if (this->is_weighted() && !other.is_weighted())
				return false;

			auto same_nodes = this->src_ == other.src_ && this->dst_ == other.dst_;
			if (!this->is_weighted() && !other.is_weighted())
				return same_nodes;
			return same_nodes && this->get_weight().value() == other.get_weight().value();
		}

		virtual ~Edge() noexcept = default;

	 protected:
		N src_;
		N dst_;

	 private:
		friend class Graph<N, E>;
	};

	template<typename N, typename E>
	class WeightedEdge : public Edge<N, E> {
	 public:
		using Edge<N, E>::Edge;

		WeightedEdge(N const& src, N const& dst, E const& weight) noexcept
		: Edge<N, E>(src, dst)
		, weight_(weight) {}

		auto print_edge() const -> std::string override {
			auto oss = std::ostringstream{};
			oss << this->src_ << " -> " << this->dst_ << " | W | " << weight_;
			return oss.str();
		}

		auto is_weighted() const noexcept -> bool override {
			return true;
		}

		auto get_weight() const noexcept -> std::optional<E> override {
			return weight_;
		}

	 private:
		E weight_;
	};

	template<typename N, typename E>
	class UnweightedEdge : public Edge<N, E> {
	 public:
		using Edge<N, E>::Edge;

		UnweightedEdge(N const& src, N const& dst) noexcept
		: Edge<N, E>(src, dst) {}

		auto print_edge() const -> std::string override {
			auto oss = std::ostringstream{};
			oss << this->src_ << " -> " << this->dst_ << " | U";
			return oss.str();
		}

		auto is_weighted() const noexcept -> bool override {
			return false;
		}

		auto get_weight() const noexcept -> std::optional<E> override {
			return std::nullopt;
		}

	 private:
	};

	template<typename N, typename E>
	class Graph {
	 public:
		class iterator {
		 public:
			using value_type = struct {
				N from;
				N to;
				std::optional<E> weight;
			};
			using reference = value_type;
			using pointer = void;
			using difference_type = std::ptrdiff_t;
			using iterator_category = std::bidirectional_iterator_tag;

			iterator() = default;

			auto operator*() const noexcept -> reference {
				const auto& edge_ptr = *iter_;
				const auto [from, to] = edge_ptr->get_nodes();
				const auto weight = edge_ptr->get_weight();
				return {from, to, weight};
			}

			auto operator++() const noexcept -> iterator& {
				++iter_;
				return const_cast<iterator&>(*this);
			}

			auto operator++(int) const noexcept -> iterator {
				auto temp = *this;
				++iter_;
				return temp;
			}

			auto operator--() const noexcept -> iterator& {
				--iter_;
				return const_cast<iterator&>(*this);
			}

			auto operator--(int) const noexcept -> iterator {
				iterator temp = *this;
				--iter_;
				return temp;
			}

			auto operator==(iterator const& other) const noexcept -> bool {
				return iter_ == other.iter_;
			}

		 private:
			mutable typename std::vector<std::unique_ptr<Edge<N, E>>>::const_iterator iter_;

			explicit iterator(typename std::vector<std::unique_ptr<Edge<N, E>>>::const_iterator it) noexcept
			: iter_{it} {}

			friend class Graph<N, E>;
		};

		Graph() = default;

		template<typename InputIt>
		Graph(InputIt first, InputIt last) {
			for (; first != last; ++first) {
				do_insert_node(*first);
			}
		}

		Graph(std::initializer_list<N> il)
		: Graph(il.begin(), il.end()) {}

		Graph(Graph&& other) noexcept {
			do_move_graph(other);
		}

		auto operator=(Graph&& other) noexcept -> Graph& {
			if (this != &other)
				do_move_graph(other);
			return *this;
		}

		Graph(Graph const& other) {
			do_copy_graph(other);
		}

		auto operator=(Graph const& other) -> Graph& {
			if (this != &other) {
				clear();
				do_copy_graph(other);
			}
			return *this;
		}

		[[nodiscard]] auto is_node(N const& value) const -> bool {
			return std::find(nodes_.begin(), nodes_.end(), value) != nodes_.end();
		}

		[[nodiscard]] auto empty() const noexcept -> bool {
			return nodes_.empty();
		}

		[[nodiscard]] auto is_connected(N const& src, N const& dst) const -> bool {
			if (!(is_node(src) && is_node(dst))) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::is_connected if src or dst node don't exist "
				                         "in the graph");
			}
			return edge_exists(src, dst, std::nullopt, false);
		}

		[[nodiscard]] auto nodes() const noexcept -> std::vector<N> {
			auto res = nodes_;
			return res;
		}

		[[nodiscard]] auto edges(N const& src, N const& dst) const -> std::vector<std::unique_ptr<Edge<N, E>>> {
			if (!(is_node(src) && is_node(dst))) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::edges if src or dst node don't exist in the "
				                         "graph");
			}

			auto res = std::vector<std::unique_ptr<Edge<N, E>>>{};
			for (const auto& e : edges_) {
				auto [first, second] = e->get_nodes();
				if (first == src && second == dst) {
					if (e->is_weighted()) {
						res.push_back(std::make_unique<WeightedEdge<N, E>>(first, second, e->get_weight().value()));
					}
					else {
						res.push_back(std::make_unique<UnweightedEdge<N, E>>(first, second));
					}
				}
			}

			std::stable_sort(res.begin(),
			                 res.end(),
			                 [this](const std::unique_ptr<Edge<N, E>>& a, const std::unique_ptr<Edge<N, E>>& b) {
				                 return this->edge_comparator(a, b);
			                 });
			return res;
		}

		[[nodiscard]] auto find(N const& src, N const& dst, std::optional<E> weight = std::nullopt) const -> iterator {
			for (auto it = begin(); it != end(); ++it) {
				if ((*it).from == src && (*it).to == dst) {
					if ((*it).weight == weight)
						return it;
				}
			}
			return end();
		}

		[[nodiscard]] auto connections(N const& src) const -> std::vector<N> {
			if (!is_node(src)) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::connections if src doesn't exist in the "
				                         "graph");
			}
			auto dsts = std::set<N>{};
			for (const auto& e : edges_) {
				auto nodes = e->get_nodes();
				if (nodes.first == src) {
					dsts.insert(nodes.second);
				}
			}
			auto res = std::vector<N>(dsts.begin(), dsts.end());
			std::sort(res.begin(), res.end());
			return res;
		}

		[[nodiscard]] auto begin() const noexcept -> iterator {
			return iterator(edges_.cbegin());
		}

		[[nodiscard]] auto end() const noexcept -> iterator {
			return iterator(edges_.cend());
		}

		[[nodiscard]] auto operator==(Graph const& other) const -> bool {
			auto a_nodes = nodes_;
			std::sort(a_nodes.begin(), a_nodes.end());
			auto b_nodes = other.nodes_;
			std::sort(b_nodes.begin(), b_nodes.end());

			if (a_nodes != b_nodes)
				return false;
			if (edges_.size() != other.edges_.size())
				return false;

			auto a_raw_edges = std::vector<Edge<N, E>*>{};
			auto b_raw_edges = std::vector<Edge<N, E>*>{};

			for (const auto& uptr : edges_) {
				a_raw_edges.push_back(uptr.get());
			}
			for (const auto& uptr : other.edges_) {
				b_raw_edges.push_back(uptr.get());
			}

			std::sort(a_raw_edges.begin(), a_raw_edges.end(), [this](Edge<N, E>* a, Edge<N, E>* b) {
				return this->edge_comparator_raw(a, b);
			});

			std::sort(b_raw_edges.begin(), b_raw_edges.end(), [&other](Edge<N, E>* a, Edge<N, E>* b) {
				return other.edge_comparator_raw(a, b);
			});

			for (auto i = std::size_t(0); i < a_raw_edges.size(); ++i) {
				if (!(*a_raw_edges[i] == *b_raw_edges[i]))
					return false;
			}

			return true;
		}

		friend auto operator<<(std::ostream& os, Graph const& g) -> std::ostream& {
			for (const auto& node : g.nodes_) {
				os << node << " (\n";

				auto edges = std::vector<Edge<N, E>*>{};
				for (const auto& e : g.edges_) {
					if (e->get_nodes().first == node) {
						edges.push_back(e.get());
					}
				}

				std::sort(edges.begin(), edges.end(), [&g](Edge<N, E>* a, Edge<N, E>* b) {
					return g.edge_comparator_raw(a, b);
				});

				for (const auto& e : edges) {
					os << "  " << e->print_edge() << "\n";
				}
				os << ")\n";
			}
			return os;
		}

		auto insert_node(N const& value) -> bool {
			if (is_node(value))
				return false;
			do_insert_node(value);
			return true;
		}

		auto insert_edge(N const& src, N const& dst, std::optional<E> weight = std::nullopt) -> bool {
			if (!(is_node(src) && is_node(dst))) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::insert_edge when either src or dst node does "
				                         "not exist");
			}
			if (edge_exists(src, dst, weight, true))
				return false;
			do_insert_edge(src, dst, weight);
			return true;
		}

		auto replace_node(N const& old_data, N const& new_data) -> bool {
			if (!is_node(old_data)) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::replace_node on a node that doesn't exist");
			}
			if (is_node(new_data))
				return false;

			std::replace(nodes_.begin(), nodes_.end(), old_data, new_data);

			for (auto& e : edges_) {
				auto [first, second] = e->get_nodes();
				if (first == old_data || second == old_data) {
					auto new_src = (first == old_data) ? new_data : first;
					auto new_dst = (second == old_data) ? new_data : second;
					if (e->is_weighted()) {
						e = std::make_unique<WeightedEdge<N, E>>(new_src, new_dst, e->get_weight().value());
					}
					else {
						e = std::make_unique<UnweightedEdge<N, E>>(new_src, new_dst);
					}
				}
			}
			return true;
		}

		auto merge_replace_node(N const& old_data, N const& new_data) -> void {
			if (!(is_node(old_data) && is_node(new_data))) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::merge_replace_node on old or new data if they "
				                         "don't exist in the graph");
			}

			auto new_edges = std::vector<std::unique_ptr<Edge<N, E>>>{};
			for (const auto& e : edges_) {
				auto [src, dst] = e->get_nodes();
				if (src != old_data && dst != old_data)
					continue;

				auto new_src = (src == old_data) ? new_data : src;
				auto new_dst = (dst == old_data) ? new_data : dst;

				if (edge_exists(new_src, new_dst, e->get_weight(), true))
					continue;

				if (e->is_weighted()) {
					new_edges.push_back(std::make_unique<WeightedEdge<N, E>>(new_src, new_dst, e->get_weight().value()));
				}
				else {
					new_edges.push_back(std::make_unique<UnweightedEdge<N, E>>(new_src, new_dst));
				}
			}

			for (auto& e : new_edges) {
				edges_.push_back(std::move(e));
			}
			do_erase_node(old_data);
		}

		auto erase_node(N const& value) -> bool {
			if (!is_node(value))
				return false;
			do_erase_node(value);
			return true;
		}

		auto erase_edge(N const& src, N const& dst, std::optional<E> weight = std::nullopt) -> bool {
			if (!(is_node(src) && is_node(dst))) {
				throw std::runtime_error("Cannot call gdwg::Graph<N, E>::erase_edge on src or dst if they don't exist "
				                         "in the graph");
			}
			auto it = std::find_if(edges_.begin(), edges_.end(), [&](const auto& edge) {
				auto [edge_src, edge_dst] = edge->get_nodes();
				if (edge_src != src || edge_dst != dst)
					return false;

				if (weight.has_value()) {
					return edge->is_weighted() && edge->get_weight() == weight;
				}
				else {
					return !edge->is_weighted();
				}
			});

			if (it == edges_.end())
				return false;
			edges_.erase(it);
			return true;
		}

		auto erase_edge(iterator i) noexcept -> iterator {
			auto next_it = edges_.erase(i.iter_);
			return iterator(next_it);
		}

		auto erase_edge(iterator i, iterator s) noexcept -> iterator {
			auto next_it = edges_.erase(i.iter_, s.iter_);
			return iterator(next_it);
		}

		auto clear() noexcept -> void {
			nodes_.clear();
			edges_.clear();
		}

		~Graph() noexcept = default;

	 private:
		std::vector<N> nodes_;
		std::vector<std::unique_ptr<Edge<N, E>>> edges_;

		auto do_move_graph(Graph&& other) noexcept -> void {
			nodes_ = std::move(other.nodes_);
			edges_ = std::move(other.edges_);
		}

		auto do_copy_graph(Graph const& other) -> void {
			nodes_ = other.nodes();
			auto o_edges = other.edges();
			for (const auto& edge : other.edges_) {
				auto [src, dst] = edge->get_nodes();
				if (edge->is_weighted()) {
					edges_.push_back(std::make_unique<WeightedEdge<N, E>>(src, dst, edge->get_weight().value()));
				}
				else {
					edges_.push_back(std::make_unique<UnweightedEdge<N, E>>(src, dst));
				}
			}
		}

		auto
		edge_exists(N const& src, N const& dst, std::optional<E> weight = std::nullopt, bool strict_check = false) const
		    -> bool {
			// strict_check = false: check for any matching edge between the two nodes
			// strict_check = true: check only for an edge between the two nodes that match the weight, or are
			// unweighted
			auto find_weighted_edge = weight.has_value();
			for (const auto& e : edges_) {
				auto [first, second] = e->get_nodes();
				if (first == src && second == dst) {
					if (!strict_check)
						return true;
					if ((find_weighted_edge && e->is_weighted() && e->get_weight() == weight.value())
					    || (!find_weighted_edge && !e->is_weighted()))
						return true;
				}
			}
			return false;
		}

		auto edge_comparator_raw(Edge<N, E>* a, Edge<N, E>* b) const noexcept -> bool {
			// sorting function used in std::sort to sort the edges_ vector
			auto [a_src, a_dst] = a->get_nodes();
			auto [b_src, b_dst] = b->get_nodes();
			const auto a_weighted = a->is_weighted();
			const auto b_weighted = b->is_weighted();

			if (a_src != b_src)
				return a_src < b_src;
			if (a_dst != b_dst)
				return a_dst < b_dst;

			if (a_weighted && b_weighted)
				return a->get_weight().value() < b->get_weight().value();
			if (!a_weighted && b_weighted)
				return true;
			if (a_weighted && !b_weighted)
				return false;
			return false;
		}

		auto edge_comparator(const std::unique_ptr<Edge<N, E>>& a, const std::unique_ptr<Edge<N, E>>& b) const noexcept
		    -> bool {
			return edge_comparator_raw(a.get(), b.get());
		}

		auto do_insert_node(N const& value) -> void {
			nodes_.push_back(value);
		}

		auto do_insert_edge(N const& src, N const& dst, std::optional<E> weight) -> void {
			edges_.push_back(
			    weight.has_value()
			        ? std::unique_ptr<Edge<N, E>>(std::make_unique<WeightedEdge<N, E>>(src, dst, weight.value()))
			        : std::unique_ptr<Edge<N, E>>(std::make_unique<UnweightedEdge<N, E>>(src, dst)));
		}

		auto do_erase_node(N const& value) -> void {
			nodes_.erase(std::remove(nodes_.begin(), nodes_.end(), value), nodes_.end());

			edges_.erase(std::remove_if(edges_.begin(),
			                            edges_.end(),
			                            [&](const auto& edge) {
				                            auto [src, dst] = edge->get_nodes();
				                            return src == value || dst == value;
			                            }),
			             edges_.end());
		}
	};
} // namespace gdwg

#endif // GDWG_GRAPH_H
